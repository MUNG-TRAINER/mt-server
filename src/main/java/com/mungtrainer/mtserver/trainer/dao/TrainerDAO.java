package com.mungtrainer.mtserver.trainer.dao;

import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainerDAO {

    // 트레이너 Id로 조회
    TrainerProfile findById(@Param("trainerId") Long trainerId);

    // 트레이너 프로필 업데이트
    int updateTrainerProfile(TrainerProfile trainer);
}
