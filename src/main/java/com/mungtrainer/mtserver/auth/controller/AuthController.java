package com.mungtrainer.mtserver.auth.controller;

import com.mungtrainer.mtserver.auth.dto.request.AuthTrainerJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.AuthUserJoinRequest;
import com.mungtrainer.mtserver.auth.dto.request.LoginRequest;
import com.mungtrainer.mtserver.auth.dto.response.AuthJoinResponse;
import com.mungtrainer.mtserver.auth.dto.response.LoginResponse;
import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.common.security.JwtTokenProvider;
import com.mungtrainer.mtserver.auth.service.AuthService;
import com.mungtrainer.mtserver.common.security.service.CustomUserDetailsService;
import com.mungtrainer.mtserver.common.util.CookieUtil;
import com.mungtrainer.mtserver.user.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<AuthJoinResponse> trainerJoin(@Valid @RequestBody AuthTrainerJoinRequest authTrainerJoinRequest) {
    AuthJoinResponse response = authService.trainerJoin(authTrainerJoinRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/join/user")
  public ResponseEntity<AuthJoinResponse> userJoin(@Valid @RequestBody AuthUserJoinRequest authUserJoinRequest) {
    AuthJoinResponse response = authService.userJoin(authUserJoinRequest);
    return ResponseEntity.ok(response);
  }


  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @RequestBody LoginRequest loginRequest,
      HttpServletResponse response
  ) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUserName(),
            loginRequest.getPassword()
        )
                                                                      );
    // 2) AT, RT 생성
    String accessToken = jwtTokenProvider.generateAccessToken(authentication);
    String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

    // 2-A) DB에 저장
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    authService.updateRefreshToken(userDetails.getUserId(), refreshToken);

    long atMaxAge =  jwtTokenProvider.getAccessTokenValidityInMs() / 1000;
    long rtMaxAge =  jwtTokenProvider.getRefreshTokenValidityInMs() / 1000;

    // 3) 쿠키 생성
    Cookie atCookie = CookieUtil.createCookie(ACCESS_TOKEN, accessToken, atMaxAge);
    Cookie rtCookie = CookieUtil.createCookie(REFRESH_TOKEN, refreshToken, rtMaxAge);

    // 4) 응답에 쿠키 추가
    response.addCookie(atCookie);
    response.addCookie(rtCookie);

    // 5) 바디 응답
    return ResponseEntity.ok(new LoginResponse("로그인 성공"));
  }

  @PostMapping("/logout")
  public ResponseEntity<LoginResponse> logout(
      @AuthenticationPrincipal CustomUserDetails principal,
      HttpServletResponse response) {
    Cookie deleteAt = CookieUtil.deleteCookie(ACCESS_TOKEN);
    Cookie deleteRt = CookieUtil.deleteCookie(ACCESS_TOKEN);

    response.addCookie(deleteAt);
    response.addCookie(deleteRt);

    authService.updateRefreshToken(principal.getUserId(), null);


    return ResponseEntity.ok(new LoginResponse("로그아웃 완료"));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponse> refresh(
      HttpServletResponse response,
      @CookieValue(value = "refresh_token", required = false) String refreshToken
  ) {
    // 1. 쿠키에 RT 없거나 무효한 JWT
    if (refreshToken == null ||
        !jwtTokenProvider.validateToken(refreshToken, JwtTokenProvider.TokenType.REFRESH)) {
      return ResponseEntity.status(401).body(new LoginResponse("유효하지 않은 리프레시 토큰입니다"));
    }

    // 2. username 추출
    String username = jwtTokenProvider.getUsername(refreshToken, JwtTokenProvider.TokenType.REFRESH);

    // 3. DB에서 해당 유저 조회
    User user = authService.findByUserName(username);

    // 4. DB에 저장된 RT와 비교
    if (!refreshToken.equals(user.getRefreshToken())) {
      return ResponseEntity.status(401).body(new LoginResponse("리프레시 토큰이 일치하지 않습니다"));
    }

    // 여기까지 통과한 RT만 재발급 가능

    // 5. 새 토큰 발급
    var userDetails = customUserDetailsService.loadUserByUsername(username);
    Authentication fakeAuth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities()
    );

    String newAT = jwtTokenProvider.generateAccessToken(fakeAuth);
    String newRT = jwtTokenProvider.generateRefreshToken(fakeAuth);

    // 6. DB에 새로운 RT 저장
    authService.updateRefreshToken(user.getUserId(), newRT);

    // 7. 쿠키 갱신
    response.addCookie(CookieUtil.createCookie("access_token", newAT,
                                               jwtTokenProvider.getAccessTokenValidityInMs() / 1000));
    response.addCookie(CookieUtil.createCookie("refresh_token", newRT,
                                               jwtTokenProvider.getRefreshTokenValidityInMs() / 1000));

    return ResponseEntity.ok(new LoginResponse("토큰 재발급 성공"));
  }
}
