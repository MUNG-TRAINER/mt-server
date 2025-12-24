package com.mungtrainer.mtserver.counseling.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequest;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponse;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponse;
import com.mungtrainer.mtserver.counseling.dto.response.DogForCounselingResponse;
import com.mungtrainer.mtserver.counseling.dto.response.UserCounselingListResponse;
import com.mungtrainer.mtserver.counseling.dto.response.UserCounselingDetailResponse;
import com.mungtrainer.mtserver.counseling.service.CounselingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/counseling")
public class CounselingUserController {
    private final CounselingService counselingService;

    // 상담 신청용 반려견 정보 조회
    @GetMapping("/dogs/{dogId}")
    public DogForCounselingResponse getDogForCounseling(
            @PathVariable("dogId") Long dogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return counselingService.getDogForCounseling(dogId, userId);
    }

    // 내 상담 목록 조회
    @GetMapping
    public List<UserCounselingListResponse> getMyCounselings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return counselingService.getUserCounselings(userId);
    }

    // 상담 상세 조회
    @GetMapping("/{counselingId}")
    public UserCounselingDetailResponse getCounselingDetail(
            @PathVariable("counselingId") Long counselingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return counselingService.getUserCounselingDetail(counselingId, userId);
    }

    // 상담 신청
    @PostMapping
//    @PreAuthorize("#userId == #userDetails.userId")
    public CreateCounselingResponse createCounseling(@Valid @RequestBody CreateCounselingRequest requestDto,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return counselingService.createCounseling(requestDto, userId);
    }


    // 상담 신청 취소
    @DeleteMapping("/{counselingId}")
    public CancelCounselingResponse cancelCounseling(
            @PathVariable("counselingId") Long counselingId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        return counselingService.cancelCounseling(counselingId, userId);

    }

}
