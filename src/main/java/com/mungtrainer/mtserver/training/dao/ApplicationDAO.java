package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.response.ApplicationListViewResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationDAO {
    // 유저 Id로 신청 리스트 조회
    List<TrainingCourseApplication> findByUserId(@Param("userId") Long userId);
    // UI 카드용 리스트 조회용
    List<ApplicationListViewResponse> findApplicationListViewByUserId(Long userId);
    // 훈련신청 상세페이지 조회
    TrainingCourseApplication findById(@Param("applicationId") Long applicationId);

    // 생성 사용자 인증
    Long findOwnerByDogId(@Param("dogId") Long dogId);

    // 생성 중복 체크
    boolean existsByDogAndSession(@Param("dogId") Long dogId, @Param("sessionId") Long sessionId);

    // 코스에 속한 세션 조회
    List<TrainingSession> findSessionsByCourseId(@Param("courseId") Long courseId);

    // 코스 상태 조회 (신청 가능 여부 확인용)
    String getCourseStatusById(@Param("courseId") Long courseId);

    // 세션 정원 조회
    int getMaxStudentsBySessionId(@Param("sessionId") Long sessionId);

    // 현재 신청 인원수 조회
    int countApplicationBySessionId(@Param("sessionId") Long sessionId);

    // 대기 테이블에 추가
    void insertWaiting(@Param("applicationId") Long applicationId, @Param("userId") Long userId);

    // 훈련과정 신청 생성
    int insertApplication(TrainingCourseApplication application);

    // 훈련과정 신청 취소 (상태 업데이트)
    void updateApplicationStatus(@Param("applicationId") Long applicationId, @Param("status") String status);

    // 세션별 대기자 조회
    List<Long> findWaitingBySessionId(@Param("sessionId") Long sessionId);

    // 대기테이블 상태 업데이트
    void updateWaitingStatus(@Param("applicationId") Long applicationId, @Param("status") String status);

    // wishlist_detail 상태 업데이트
    void updateWishlistDetailStatus(@Param("wishlistItemId") Long wishlistItemId, @Param("status") String status);

    // 전체취소
    void updateApplicationStatusBatch(@Param("applicationIds") List<Long> applicationIds, @Param("status") String status);
    // 전체취소시 웨이팅 처리
    void updateWaitingStatusBatch(@Param("applicationIds") List<Long> applicationIds, @Param("status") String status);

    // 해당 강아지 상담 여부 가져오기
    boolean findCounselingByDogID(@Param("dogId") Long dogId);

    // 세션 status 확인용
    TrainingSession findSessionById(Long sessionId);
    // session status = done 일 경우 application status = EXPIRED
    void updateApplicationStatusIfNotExpired(@Param("applicationId") Long applicationId, @Param("status") String status);
//    유저아이디와 코스아이디로 application 찾기
    List<TrainingCourseApplication> findApplicationsByUserAndCourses(
            @Param("userId") Long userId,
            @Param("courseIds") List<Long> courseIds
    );

    List<Long> findSessionIdsByCourseIds(
            @Param("courseIds") List<Long> courseIds
    );
    List<TrainingCourseApplication> findApplicationsByUserAndSessions(
            @Param("userId") Long userId,
            @Param("sessionIds") List<Long> sessionIds
    );
    List<TrainingCourseApplication> findCancelableApplicationsByUserAndCourses(
            @Param("userId") Long userId,
            @Param("courseIds") List<Long> courseIds,
            @Param("dogId") Long dogId  // 특정 반려견의 신청만 조회
    );

    /**
     * applicationId로 취소 가능한 신청 조회 (소유권 및 상태 검증 포함)
     */
    List<TrainingCourseApplication> findCancelableApplicationsByIds(
            @Param("userId") Long userId,
            @Param("applicationIds") List<Long> applicationIds
    );

}
