package com.mungtrainer.mtserver.counseling.controller;

import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequestDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponseDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponseDTO;
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
    public CreateCounselingResponseDTO createCounseling(@Valid @RequestBody CreateCounselingRequestDTO requestDto
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
    public CancelCounselingResponseDTO cancelCounseling(
            @PathVariable("counselingId") Long counselingId
//            @AuthenticationPrincipal CustomUserDetail userDetails
    ) {
//        return counselingService.cancelCounseling(counselingId, userDetails.getUserId());
        // 테스트용 하드코딩 userId
        Long userId = 1L;
        return counselingService.cancelCounseling(counselingId, userId);

    }

}
