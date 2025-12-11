package com.mungtrainer.mtserver.user.controller;

import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 프로필 조회
     * GET /api/users/{userId}/profile
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 프로필 수정
     * PUT /api/users/{userId}/profile
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    //훈련사 프로필 조회
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<TrainerResponse> getTrainerProfileById(@PathVariable Long trainerId){
        TrainerResponse profile = userService.getTrainerProfileById(trainerId);
        return ResponseEntity.ok(profile);
    }

}