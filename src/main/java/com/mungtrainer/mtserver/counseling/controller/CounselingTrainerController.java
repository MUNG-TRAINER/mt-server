package com.mungtrainer.mtserver.counseling.controller;

import com.mungtrainer.mtserver.counseling.dto.request.CounselingPostRequestDto;
import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.counseling.service.CounselingService;
import com.mungtrainer.mtserver.counseling.service.TrainerUserService;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class CounselingTrainerController {

    private final CounselingService counselingService;
    private final TrainerUserService trainerService;

    // 상담 완료 전 후 리스트 조회
    @GetMapping("/counseling")
    public List<CounselingDogResponseDto> getCounselingDogs(
            @RequestParam boolean completed
            ) {
        return counselingService.getDogsByCompleted(completed);
    }

    // 상담 내용 작성 (훈련사 본인만 가능)
    @PatchMapping("/counseling/{counselingId}")
    public ResponseEntity<CounselingPostResponseDto> addCounselingContent(
            @PathVariable("counselingId") Long counselingId,
            @RequestBody CounselingPostRequestDto requestDto
    ) {  // 로그인한 훈련사 정보

//        CounselingResponseDto response = counselingService.addCounselingContent(
//                counselingId, requestDto, userDetails.getUserId());

        // 테스트용 하드코딩 userId
        Long userId = 2L;
        CounselingPostResponseDto response = counselingService.addCounselingContent(
                counselingId, requestDto, userId);


        return ResponseEntity.ok(response);
    }

    // 훈련사가 관리하는 회원 목록 조회
    @GetMapping("/users/{trainerId}")
    public List<TrainerUserListResponseDto> getTrainerUsers(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long trainerId
    ) {
//        Long trainerId = userDetails.getUserId();
        return trainerService.getUsersByTrainer(trainerId);
    }

    // 내 회원의 반려견 목록 조회
    @GetMapping("/dogs/{userId}")
    public List<DogResponse> getDogList(
            @PathVariable Long userId
    ) {
        return trainerService.getDogsByUser(userId);
    }

    // <=============== 반려견 통계 페이지 조회 ========================>
    // 목록 조회 → 반려견이 신청했던 모든 훈련 정보를 요약해서 보여주는 가벼운 쿼리
    @GetMapping("/user/dogs/{dogId}")
    public ResponseEntity<DogStatsResponseDto> getDogStats(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("dogId") Long dogId
    ) {
//        Long trainerId = userDetails.getUserId();
        // 테스트용 하드코딩 trainerId
        Long trainerId = 2L;
        DogStatsResponseDto dogStats = trainerService.getDogStats(dogId, trainerId);
        return ResponseEntity.ok(dogStats);
    }



}
