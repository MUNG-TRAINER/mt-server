package com.mungtrainer.mtserver.user.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.user.dto.request.UserImageUploadRequest;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserImageUploadResponse;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 프로필 조회
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UserProfileResponse response = userService.getUserProfile(customUserDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 프로필 수정
     * PUT /api/users
     */
    @PatchMapping
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userService.updateUserProfile(customUserDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지 업로드용 Presigned URL 발급
     * POST /api/users/upload-url
     */
    @PostMapping("/upload-url")
    public ResponseEntity<UserImageUploadResponse> generateUploadUrl(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody UserImageUploadRequest request) {
        UserImageUploadResponse response = userService.generateUploadUrl(customUserDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }
}