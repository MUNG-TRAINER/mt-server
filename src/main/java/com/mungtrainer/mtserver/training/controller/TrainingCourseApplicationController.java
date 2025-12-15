package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.service.TrainingCourseApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class TrainingCourseApplicationController {

    private final TrainingCourseApplicationService applicationService;

    // 훈련과정 신청 리스트
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplicationList(@AuthenticationPrincipal CustomUserDetails principal){
        Long userId = principal.getUserId();
        List<ApplicationResponse> applicationList = applicationService.getApplicationsByUserId(userId);
        return ResponseEntity.ok(applicationList);
    }

    // 훈련과정 신청 상세페이지
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplication(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long applicationId){
        Long userId = principal.getUserId();
        ApplicationResponse application = applicationService.getApplicationById( userId,applicationId);
        return ResponseEntity.ok(application);
    }

     // 훈련과정 신청 생성
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@AuthenticationPrincipal CustomUserDetails principal, @RequestBody ApplicationRequest request ){
        Long userId = principal.getUserId();
        ApplicationResponse created = applicationService.createApplication(userId,request,  request.getWishlistItemId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 훈련과정 신청 취소
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long applicationId){
        Long userId = principal.getUserId();
        applicationService.cancelApplication(userId,applicationId);
        return ResponseEntity.ok().build();
    }

}
