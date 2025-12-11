package com.mungtrainer.mtserver.counseling.controller;

import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequestDto;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponseDto;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponseDto;
import com.mungtrainer.mtserver.counseling.service.CounselingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/counseling")
public class CounselingUserController {
    private final CounselingService counselingService;

    // 상담 신청
    @PostMapping
//    @PreAuthorize("#userId == #userDetails.userId")
    public CreateCounselingResponseDto createCounseling(@Valid @RequestBody CreateCounselingRequestDto requestDto
//                                                        @AuthenticationPrincipal
//                                                        CustomUserDetail userDetails
    ) {
//        Long userId = userDetails.getUserId();

        // 테스트용 하드코딩 userId
        Long userId = 1L;
        return counselingService.createCounseling(requestDto, userId);
    }


    // 상담 신청 취소
    @DeleteMapping("/{counselingId}")
    public CancelCounselingResponseDto cancelCounseling(
            @PathVariable("counselingId") Long counselingId
//            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
//        return counselingService.cancelCounseling(counselingId, userDetails.getUserId());
        // 테스트용 하드코딩 userId
        Long userId = 1L;
        return counselingService.cancelCounseling(counselingId, userId);

    }

}
