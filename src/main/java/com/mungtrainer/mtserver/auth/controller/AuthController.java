package com.mungtrainer.mtserver.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mungtrainer.mtserver.auth.dto.request.AuthTrainerJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.AuthUserJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.FCMRequest;
import com.mungtrainer.mtserver.auth.dto.request.LoginRequest;
import com.mungtrainer.mtserver.auth.dto.request.PasswordChangeRequest;
import com.mungtrainer.mtserver.auth.dto.response.AuthDuplicatedCheckResponse;
import com.mungtrainer.mtserver.auth.dto.response.AuthJoinResponse;
import com.mungtrainer.mtserver.auth.dto.response.CheckResponse;
import com.mungtrainer.mtserver.auth.dto.response.LoginResponse;
import com.mungtrainer.mtserver.auth.dto.response.PasswordChangeResponse;
import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.auth.service.AuthService;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.security.JwtTokenProvider;
import com.mungtrainer.mtserver.common.security.service.CustomUserDetailsService;
import com.mungtrainer.mtserver.common.util.CookieUtil;
import com.mungtrainer.mtserver.user.entity.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthService authService;
	private final CustomUserDetailsService customUserDetailsService;

	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";

	@PostMapping("/join/trainer")
	public ResponseEntity<AuthJoinResponse> trainerJoin(@Valid
	@RequestBody
	AuthTrainerJoinRequest authTrainerJoinRequest) {
		AuthJoinResponse response = authService.trainerJoin(authTrainerJoinRequest);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/join/user")
	public ResponseEntity<AuthJoinResponse> userJoin(@Valid
	@RequestBody
	AuthUserJoinRequest authUserJoinRequest) {
		AuthJoinResponse response = authService.userJoin(authUserJoinRequest);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
		@Valid
		@RequestBody
		LoginRequest loginRequest,
		HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
				loginRequest.getUserName(),
				loginRequest.getPassword()));
		// 2) AT, RT 생성
		String accessToken = jwtTokenProvider.generateAccessToken(authentication);
		String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

		// 2-A) DB에 저장
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();
		authService.updateRefreshToken(userDetails.getUserId(), refreshToken);

		long atMaxAge = jwtTokenProvider.getAccessTokenValidityInMs() / 1000;
		long rtMaxAge = jwtTokenProvider.getRefreshTokenValidityInMs() / 1000;

		// 3) 쿠키 생성
		Cookie atCookie = CookieUtil.createCookie(ACCESS_TOKEN, accessToken, atMaxAge);
		Cookie rtCookie = CookieUtil.createCookie(REFRESH_TOKEN, refreshToken, rtMaxAge);

		// 4) 응답에 쿠키 추가
		response.addCookie(atCookie);
		response.addCookie(rtCookie);

		// 5) 바디 응답
		return ResponseEntity.ok(new LoginResponse(200, "Success", "로그인에 성공했습니다."));
	}

	@GetMapping("/logout")
	public ResponseEntity<LoginResponse> logout(
		@AuthenticationPrincipal
		CustomUserDetails principal,
		HttpServletResponse response) {
		Cookie deleteAt = CookieUtil.deleteCookie(ACCESS_TOKEN);
		Cookie deleteRt = CookieUtil.deleteCookie(REFRESH_TOKEN);

		response.addCookie(deleteAt);
		response.addCookie(deleteRt);

		authService.updateRefreshToken(principal.getUserId(), null);

		return ResponseEntity.ok(new LoginResponse(200, "Success", "로그아웃에 성공했습니다."));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<LoginResponse> refresh(
		HttpServletResponse response,
		@CookieValue(value = "refresh_token", required = false)
		String refreshToken) {
		int status;
		String code;
		String message;
		// 1. 쿠키에 RT 없거나 무효한 JWT
		if (refreshToken == null ||
			!jwtTokenProvider.validateToken(refreshToken, JwtTokenProvider.TokenType.REFRESH)) {
			ErrorCode errorCode = ErrorCode.REFRESH_EXPIRED;
			status = errorCode.getStatus();
			code = errorCode.name();
			message = errorCode.getMessage();
			return ResponseEntity.status(401).body(new LoginResponse(status, code, message));
		}

		// 2. username 추출
		String username = jwtTokenProvider.getUsername(refreshToken, JwtTokenProvider.TokenType.REFRESH);

		// 3. DB에서 해당 유저 조회
		User user = authService.findByUserName(username);

		// 4. DB에 저장된 RT와 비교
		if (!refreshToken.equals(user.getRefreshToken())) {
			ErrorCode errorCode = ErrorCode.REFRESH_EXPIRED;
			status = errorCode.getStatus();
			code = errorCode.name();
			message = errorCode.getMessage();
			return ResponseEntity.status(401).body(new LoginResponse(status, code, message));
		}

		// 여기까지 통과한 RT만 재발급 가능

		// 5. 새 토큰 발급
		var userDetails = customUserDetailsService.loadUserByUsername(username);
		Authentication refreshedAuthentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities());

		String newAT = jwtTokenProvider.generateAccessToken(refreshedAuthentication);
		String newRT = jwtTokenProvider.generateRefreshToken(refreshedAuthentication);

		// 6. DB에 새로운 RT 저장
		authService.updateRefreshToken(user.getUserId(), newRT);

		// 7. 쿠키 갱신
		response.addCookie(CookieUtil.createCookie("access_token", newAT,
			jwtTokenProvider.getAccessTokenValidityInMs() / 1000));
		response.addCookie(CookieUtil.createCookie("refresh_token", newRT,
			jwtTokenProvider.getRefreshTokenValidityInMs() / 1000));

		return ResponseEntity.ok(new LoginResponse(200, "Success", "토큰 재발급에 성공했습니다."));
	}

	@GetMapping("/check-email")
	public ResponseEntity<AuthDuplicatedCheckResponse> emailDuplicatedCheck(@RequestParam("email")
	String email) {
		return ResponseEntity.ok(authService.emailDuplicatedCheck(email));
	}

	@GetMapping("/check-username")
	public ResponseEntity<AuthDuplicatedCheckResponse> userNameDuplicatedCheck(@RequestParam("username")
	String userName) {
		return ResponseEntity.ok(authService.userNameDuplicatedCheck(userName));
	}

	@PostMapping("/password")
	public ResponseEntity<PasswordChangeResponse> changePassword(
		@Valid
		@RequestBody
		PasswordChangeRequest request,
		@AuthenticationPrincipal
		CustomUserDetails principal) {
		String userName = principal.getUsername();
		authService.passwordChange(request, userName);
		return ResponseEntity.ok(new PasswordChangeResponse("Success", 200, "비밀번호가 성공적으로 변경되었습니다."));
	}

	@GetMapping("/check")
	public ResponseEntity<CheckResponse> checkAuthentication(
		@AuthenticationPrincipal
		CustomUserDetails principal) {
		log.info("checkAuthentication called");
		log.info("principal: {},{},{}", principal, principal.getUserId(), principal.getUsername());
		return ResponseEntity.ok(CheckResponse.builder()
			.userId(principal.getUserId())
			.role(principal.getRole())
			.username(principal.getUsername())
			.build());
	}

	@PatchMapping("/fcm-token")
	public ResponseEntity<Void> updateFcmToken(
		@RequestBody
		FCMRequest request,
		@AuthenticationPrincipal
		CustomUserDetails principal) {
		if (principal == null) {
			log.info("FCM token received but user not authenticated");
			return ResponseEntity.noContent().build();
		}
		Long userId = principal.getUserId();
		String username = principal.getUsername();
		String fcmToken = request.getFcmToken();
		if (fcmToken == null || fcmToken.isBlank()) {
			return ResponseEntity.noContent().build();
		}
		authService.updateFcmToken(userId, username, fcmToken);
		return ResponseEntity.noContent().build();
	}
}
