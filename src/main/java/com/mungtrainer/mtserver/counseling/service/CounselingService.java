package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.counseling.dao.CounselingDao;
import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequestDto;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponseDto;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponseDto;
import com.mungtrainer.mtserver.counseling.entity.Counseling;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CounselingService {
    private final CounselingDao counselingDao;

    public CreateCounselingResponseDto createCounseling(CreateCounselingRequestDto requestDto, Long userId){
        Counseling counseling = Counseling.builder()
                .dogId(requestDto.getDogId())
                .phone(requestDto.getPhone())
                .isCompleted(false) // 상담 완료 전
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        int result = counselingDao.insertCounseling(counseling);

        if (result == 0) {
            throw new RuntimeException("상담 신청에 실패했습니다.");
        }

        return new CreateCounselingResponseDto(counseling.getCounselingId(), "상담신청이 성공적으로 신청되었습니다.");
    }


    /**
     * 상담 취소
     * @param counselingId 취소할 상담 ID
     * @return 취소 성공 여부 메시지
     */

    public CancelCounselingResponseDto cancelCounseling(Long counselingId){
        // SQL 업데이트 후 영향받은 행 수 반환 int
        int result = counselingDao.cancelCounseling(counselingId);
        if (result == 0) {
            return new CancelCounselingResponseDto(false, "이미 취소된 상담이거나 존재하지 않는 상담입니다.");
        } else {
            return new CancelCounselingResponseDto(true, "상담이 성공적으로 취소되었습니다.");
        }
    }
}
