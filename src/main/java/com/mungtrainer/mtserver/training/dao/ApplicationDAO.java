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
    // 신청 강아지 변경
    void updateApplicationDog(@Param("applicationId") Long applicationId,
                              @Param("newDogId") Long newDogId);

    // 전체취소
    void updateApplicationStatusBatch(@Param("applicationIds") List<Long> applicationIds, @Param("status") String status);
    // 전체취소시 웨이팅 처리
    void updateWaitingStatusBatch(@Param("applicationIds") List<Long> applicationIds, @Param("status") String status);

    // 해당 강아지 상담 여부 가져오기
    boolean findCounselingByDogID(@Param("dogId") Long dogId);

    // 세션 status 확인용
    TrainingSession findSessionById(Long sessionId);
    // 세션 스테이터스 업데이트
    void updateSessionStatusIfNotDone(@Param("sessionId") Long sessionId, @Param("status") String status);

    void updateApplicationStatusIfNotExpired(@Param("applicationId") Long applicationId, @Param("status") String status);


}
