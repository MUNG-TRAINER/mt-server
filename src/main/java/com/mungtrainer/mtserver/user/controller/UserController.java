package com.mungtrainer.mtserver.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.service.TrainerService;
import com.mungtrainer.mtserver.user.dto.request.UpdatePublicStatusRequest;
import com.mungtrainer.mtserver.user.dto.request.UserImageUploadRequest;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserFCMTokenResponse;
import com.mungtrainer.mtserver.user.dto.response.UserImageUploadResponse;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final TrainerService trainerService;

	/**
	 * 내 프로필 조회 (전체 정보)
	 * GET /api/users/me
	 */
	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getMyProfile(
		@AuthenticationPrincipal
		CustomUserDetails customUserDetails) {
		UserProfileResponse response = userService.getUserProfile(customUserDetails.getUserId());
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자명으로 프로필 조회 (권한에 따라 다른 정보 반환)
	 * GET /api/users/{userName}
	 */
	@GetMapping("/{userName}")
	public ResponseEntity<UserProfileResponse> getUserProfileByUserName(
		@PathVariable
		String userName,
		@AuthenticationPrincipal
		CustomUserDetails customUserDetails) {
		UserProfileResponse response = userService.getUserProfileByUserName(
			userName,
			customUserDetails.getUserId(),
			customUserDetails.getRole());
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자 프로필 수정
	 * PUT /api/users
	 */
	@PutMapping("/me")
	public ResponseEntity<UserProfileResponse> updateUserProfile(
		@AuthenticationPrincipal
		CustomUserDetails customUserDetails,
		@Valid
		@RequestBody
		UserProfileUpdateRequest request) {
		UserProfileResponse response = userService.updateUserProfile(customUserDetails.getUserId(), request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 프로필 이미지 업로드용 Presigned URL 발급
	 * POST /api/users/upload-url
	 */
	@PostMapping("/upload-url")
	public ResponseEntity<UserImageUploadResponse> generateUploadUrl(
		@AuthenticationPrincipal
		CustomUserDetails customUserDetails,
		@Valid
		@RequestBody
		UserImageUploadRequest request) {
		UserImageUploadResponse response = userService.generateUploadUrl(customUserDetails.getUserId(), request);
		return ResponseEntity.ok(response);
	}

	//훈련사 프로필 조회
	@GetMapping("/trainer/{trainerId}")
	public ResponseEntity<TrainerResponse> getTrainerProfileById(@PathVariable
	Long trainerId) {
		TrainerResponse profile = trainerService.getTrainerProfileById(trainerId);
		return ResponseEntity.ok(profile);
	}

	/**
	 * 사용자 반려견 프로필 공개 여부 변경
	 * PATCH /api/users/me/public-status
	 */
	@PatchMapping("/me/public-status")
	public ResponseEntity<Void> updatePublicStatus(
		@AuthenticationPrincipal
		CustomUserDetails customUserDetails,
		@Valid
		@RequestBody
		UpdatePublicStatusRequest request) {
		userService.updatePublicStatus(customUserDetails.getUserId(), request.getIsPublic());
		return ResponseEntity.ok().build();
	}

	// 유저의 FCM Token 조회
	@GetMapping("/fcm-token/{userId}")
	public ResponseEntity<UserFCMTokenResponse> findUserFCMToken(
		@PathVariable
		Long userId) {
		UserFCMTokenResponse userFCMToken = userService.findUserFCMToken(userId);
		return ResponseEntity.ok(userFCMToken);
	}

}