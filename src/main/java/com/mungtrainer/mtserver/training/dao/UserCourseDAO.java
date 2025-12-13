package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponse;
import com.mungtrainer.mtserver.training.dto.response.UserCourseResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCourseDAO {
    List<UserCourseResponse> selectUserCourses(
            @Param("userId") Long userId,
            @Param("sessionStatus") String sessionStatus
    );
}
