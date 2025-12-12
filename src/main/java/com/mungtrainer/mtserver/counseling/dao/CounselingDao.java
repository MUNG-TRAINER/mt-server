package com.mungtrainer.mtserver.counseling.dao;

import com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponseDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingResponseDTO;
import com.mungtrainer.mtserver.counseling.entity.Counseling;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CounselingDao {
    // 상담 등록
    int insertCounseling(Counseling counseling);

    // 상담 취소
    int cancelCounseling(@Param("counselingId") Long counselingId);

    // 상담 조회
    Counseling findById(@Param("counselingId") Long counselingId);

    // 상담 완료 여부별 반려견/보호자 리스트 조회
    List<CounselingDogResponseDTO> findDogsByCompleted(@Param("completed") boolean completed);

    // 상담 내용 업데이트 + 완료 처리
    int updateContentAndComplete(@Param("counselingId") Long counselingId,
                                 @Param("content") String content,
                                 @Param("trainerId") Long trainerId);

    /**
     * 특정 반려견과 훈련사 기준 상담 내역 조회
     */
    List<CounselingResponseDTO> selectCounselingsByDogAndTrainer(
            @Param("dogId") Long dogId
    );
}
