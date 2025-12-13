package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import org.apache.ibatis.annotations.Mapper;

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
}