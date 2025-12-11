package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.service.TrainingCourseApplicationService;
import com.mungtrainer.mtserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class TrainingCourseApplicationController {

    private final TrainingCourseApplicationService applicationService;

    // 훈련과정 신청 리스트
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplicationList(@RequestAttribute("user") User user){
        List<ApplicationResponse> applicationList = applicationService.getApplicationsByUserId(user.getUserId());
        return ResponseEntity.ok(applicationList);
    }

    // 훈련과정 신청 상세페이지
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getApplication(@RequestAttribute("user") User user, @PathVariable Long applicationId){
        ApplicationResponse application = applicationService.getApplicationById( user.getUserId(),applicationId);
        return ResponseEntity.ok(application);
    }

    // 훈련과정 신청 생성
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@RequestAttribute("user") User user, @RequestBody ApplicationRequest request){
        ApplicationResponse created = applicationService.createApplication(user.getUserId(),request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 훈련과정 신청 취소
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@RequestAttribute("user") User user, @PathVariable Long applicationId){
        applicationService.cancelApplication(user.getUserId(),applicationId);
        return ResponseEntity.ok().build();
    }

}
