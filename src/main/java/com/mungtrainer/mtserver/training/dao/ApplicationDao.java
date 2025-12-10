package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationDao {
    // 유저 Id로 신청 리스트 조회
    List<TrainingCourseApplication> findByUserId(@Param("userId") Long userId);

    // 훈련신청 상세페이지 조회
    TrainingCourseApplication findById(@Param("applicationId") Long applicationId);

    // Mapper XML 또는 인터페이스
    boolean existsByDogAndSession(@Param("dogId") Long dogId, @Param("sessionId") Long sessionId);

    // 훈련과정 신청 생성
    int insertApplication(TrainingCourseApplication application);

    // 훈련과정 신청 취소 (상태 업데이트)
    void updateApplicationStatus(@Param("applicationId") Long applicationId, @Param("status") String status);
}
