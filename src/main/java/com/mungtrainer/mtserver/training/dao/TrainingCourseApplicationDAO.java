package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 훈련과정 신청 Mapper
 */
@Mapper
public interface TrainingCourseApplicationDAO {

    /**
     * 신청서 ID로 조회
     */
    TrainingCourseApplication findById(Long applicationId);

    /**
     * 신청서 상태 업데이트
     */
    void updateStatus(TrainingCourseApplication application);

    /**
     * 진행 중인 훈련 신청 존재 여부 확인
     * @param dogId 반려견 ID
     * @return 존재 여부
     */
    boolean existsActiveByDogId(@Param("dogId") Long dogId);
}