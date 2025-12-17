package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.order.dto.request.WishlistDeleteRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistUpdateRequest;
import com.mungtrainer.mtserver.training.dto.request.ApplicationCancelRequest;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationListViewResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationStatusResponse;
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

    // 훈련과정 내역 조회
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

    // 훈련과정 신청 삭제 (여러 개)
    @DeleteMapping
    public ResponseEntity<Void> deleteApplicationList( @AuthenticationPrincipal CustomUserDetails principal,@RequestBody ApplicationCancelRequest request) {
        Long userId = principal.getUserId();
        applicationService.deleteApplicationList(userId, request);
        return ResponseEntity.ok().build();
    }


//    // 신청내역 리스트 (UI 카드용)
//    @GetMapping("/list")
//    public ResponseEntity<List<ApplicationListViewResponse>> getApplicationListView(@AuthenticationPrincipal CustomUserDetails principal) {
//        Long userId = principal.getUserId();
//        return ResponseEntity.ok(
//                applicationService.getApplicationListView(userId)
//        );
//    }

    // 신청내역 리스트 (UI 카드용)
    @GetMapping("/list")
    public ResponseEntity<List<ApplicationListViewResponse>> getApplicationListView() {
        Long userId = 4L;
        return ResponseEntity.ok(
                applicationService.getApplicationListView(userId)
        );
    }

    @GetMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationStatus(
                        principal.getUserId(), applicationId
                )
        );
    }

    // 신청 강아지 수정
    @PatchMapping("/{applicationId}")
    public ResponseEntity<Void> updateApplicationDog(@PathVariable Long applicationId, @RequestBody WishlistUpdateRequest request, @AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal.getUserId();
        applicationService.updateApplicationDog(userId, applicationId, request.getDogId());
        return ResponseEntity.ok().build();
    }
}
