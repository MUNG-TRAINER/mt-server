package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.request.UpdateSessionRequest;
import com.mungtrainer.mtserver.training.dto.response.CalendarSessionDateDto;
import com.mungtrainer.mtserver.training.dto.response.TrainingSessionResponse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TrainingSessionDAO {

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
    int updateSession(@Param("request") UpdateSessionRequest request,
                      @Param("sessionId") Long sessionId);

    /**
     * 세션 삭제 (Soft Delete)
     */
    int deleteSession(@Param("sessionId") Long sessionId);

    /**
     * 세션에 신청자가 있는지 확인
     */
    Boolean hasActiveApplications(@Param("sessionId") Long sessionId);

    /**
     * 세션 ID로 조회
     */
    TrainingSession findById(Long sessionId);

    /**
     * 결제 완료된 신청이 있는지 확인
     */
    Boolean hasPaidApplications(@Param("sessionId") Long sessionId);

    /**
     * 피드백 첨부파일 삭제
     */
    int deleteFeedbackAttachmentsBySessionId(@Param("sessionId") Long sessionId,
                                               @Param("deletedBy") Long deletedBy);

    /**
     * 피드백 삭제
     */
    int deleteFeedbacksBySessionId(@Param("sessionId") Long sessionId,
                                     @Param("deletedBy") Long deletedBy);

    /**
     * 출석 정보 삭제
     */
    int deleteAttendancesBySessionId(@Param("sessionId") Long sessionId,
                                       @Param("deletedBy") Long deletedBy);

    /**
     * 대기 정보 삭제
     */
    int deleteWaitingBySessionId(@Param("sessionId") Long sessionId,
                                   @Param("deletedBy") Long deletedBy);

    /**
     * 신청 정보 삭제
     */
    int deleteApplicationsBySessionId(@Param("sessionId") Long sessionId,
                                        @Param("deletedBy") Long deletedBy);

    /**
     * 세션 공지사항 삭제
     */
    int deleteNoticesBySessionId(@Param("sessionId") Long sessionId,
                                   @Param("deletedBy") Long deletedBy);

    /**
     * 세션 변경 이력 삭제
     */
    int deleteSessionChangesBySessionId(@Param("sessionId") Long sessionId,
                                          @Param("deletedBy") Long deletedBy);

    /**
     * 세션 삭제
     */
    int deleteSessionById(@Param("sessionId") Long sessionId,
                          @Param("deletedBy") Long deletedBy);

    /**
     * 특정 기간의 세션 날짜 목록 조회 (달력용)
     */
    List<CalendarSessionDateDto> findSessionDatesByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("trainerId") Long trainerId,
            @Param("keyword") String keyword,
            @Param("lessonForm") String lessonForm);
}

