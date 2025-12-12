package com.mungtrainer.mtserver.trainer.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.trainer.dto.request.TrainerProfileUpdateRequest;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.service.TrainerService;
import com.mungtrainer.mtserver.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    @GetMapping("/me/certification-upload-url")
    public ResponseEntity<String> getCertificationUploadUrl(
            @RequestParam String contentType,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal.getUserId();
        // 입력 검증
        if(contentType == null || contentType.isBlank()) {
            return ResponseEntity.badRequest().body("contentType은 필수입니다.");
        }

        // 허용된 MIME 타입만
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            return ResponseEntity.badRequest().body("허용되지 않은 파일 형식입니다. (image/jpeg, image/png)");
        }

        String presignedUrl = trainerService.generateCertificationUploadUrl(userId, contentType);
        return ResponseEntity.ok(presignedUrl);
    }

    //프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<TrainerResponse> updateTrainerProfile(@RequestBody TrainerProfileUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal){
        // principal에서 로그인한 사용자 ID 가져오기
        Long userId = principal.getUserId();
        TrainerResponse profile = trainerService.updateTrainerProfile(request, userId);
        return ResponseEntity.ok(profile);
    }

}