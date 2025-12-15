package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationDAO {
    // 유저 Id로 신청 리스트 조회
    List<TrainingCourseApplication> findByUserId(@Param("userId") Long userId);

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
}
