package com.mungtrainer.mtserver.counseling.dao;

import com.mungtrainer.mtserver.counseling.dto.response.TrainerUserListResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainerUserDao {

    // 회원 목록 조회
    List<TrainerUserListResponseDto> findUsersByTrainerId(@Param("trainerId") Long trainerId);
}
