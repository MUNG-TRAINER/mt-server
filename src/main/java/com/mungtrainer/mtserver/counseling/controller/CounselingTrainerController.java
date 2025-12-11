package com.mungtrainer.mtserver.counseling.controller;

import com.mungtrainer.mtserver.counseling.dto.request.CounselingPostRequestDto;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponseDto;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingPostResponseDto;
import com.mungtrainer.mtserver.counseling.dto.response.TrainerUserListResponseDto;
import com.mungtrainer.mtserver.counseling.service.CounselingService;
import com.mungtrainer.mtserver.counseling.service.TrainerUserService;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public List<CounselingDogResponseDto> getCounselingDogs(@RequestParam boolean completed) {
        return counselingService.getDogsByCompleted(completed);
    }

    // 상담 내용 작성
    @PostMapping("/counseling/{counselingId}")
    public ResponseEntity<CounselingPostResponseDto> addCounselingContent(
            @PathVariable("counselingId") Long counselingId,
            @RequestBody CounselingPostRequestDto requestDto
//            ,@AuthenticationPrincipal UserPrincipal user
    ) {  // 로그인한 훈련사 정보

//        CounselingResponseDto response = counselingService.addCounselingContent(
//                counselingId, requestDto, user.getId());

        // 테스트용 하드코딩 userId
        Long userId = 2L;
        CounselingPostResponseDto response = counselingService.addCounselingContent(
                counselingId, requestDto, userId);


        return ResponseEntity.ok(response);
    }

    // 훈련사가 관리하는 회원 목록 조회
    @GetMapping("/users/{trainerId}")
    public List<TrainerUserListResponseDto> getTrainerUsers(
            @PathVariable Long trainerId
    ) {
        return trainerService.getUsersByTrainer(trainerId);
    }

    // 내 회원의 반려견 목록 조회
    @GetMapping("/dogs/{userId}")
    public List<DogResponse> getDogList(
//            @AuthenticationPrincipal JwtUser user,
            @PathVariable Long userId
    ) {
//        return trainerService.getDogsByTrainer(user.getId(), userId);
        // 테스트용: 항상 userId = 2L 로 조회
        Long fixedUserId = 1L;
        return trainerService.getDogsByUser(fixedUserId);
    }

}
