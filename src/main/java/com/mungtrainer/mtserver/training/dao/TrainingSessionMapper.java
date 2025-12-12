package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.request.UpdateSessionRequest;
import com.mungtrainer.mtserver.training.dto.response.TrainingSessionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingSessionMapper {

    /**
     * 특정 코스의 세션 목록 조회
     */
    List<TrainingSessionResponse> findSessionsByCourseId(@Param("courseId") Long courseId);

    /**
     * 특정 세션 상세 조회
     */
    TrainingSessionResponse findSessionById(@Param("courseId") Long courseId,
                                           @Param("sessionId") Long sessionId);

    /**
     * 세션이 해당 훈련사의 것인지 확인
     */
    Boolean isSessionOwnedByTrainer(@Param("sessionId") Long sessionId,
                                    @Param("trainerId") Long trainerId);

    /**
     * 세션 정보 수정
     */
    void updateSession(@Param("request") UpdateSessionRequest request,
                       @Param("sessionId") Long sessionId);

    /**
     * 세션 삭제 (Soft Delete)
     */
    void deleteSession(@Param("sessionId") Long sessionId);

    /**
     * 세션에 신청자가 있는지 확인
     */
    Boolean hasActiveApplications(@Param("sessionId") Long sessionId);
}