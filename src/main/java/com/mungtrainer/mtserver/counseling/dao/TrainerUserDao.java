package com.mungtrainer.mtserver.counseling.dao;

import com.mungtrainer.mtserver.counseling.dto.response.MultiCourseGroupDto;
import com.mungtrainer.mtserver.counseling.dto.response.MultiSessionDto;
import com.mungtrainer.mtserver.counseling.dto.response.TrainerUserListResponseDto;
import com.mungtrainer.mtserver.counseling.dto.response.TrainingApplicationResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainerUserDao {

    // 회원 목록 조회
    List<TrainerUserListResponseDto> findUsersByTrainerId(@Param("trainerId") Long trainerId);
    // 반려견 신청 과정 조회 (출석 미포함, tags 기준 회차 계산)
    List<TrainingApplicationResponseDto> findTrainingApplicationsByDogId(@Param("dogId") Long dogId);

    List<MultiCourseGroupDto> findMultiCoursesByDogId(Long dogId);

    List<MultiSessionDto> findSessionsWithAttendance(
            @Param("dogId") Long dogId,
            @Param("courseId") Long courseId
    );

    int countSessionsByCourseId(Long courseId);

    // 출석한 세션 수 조회 (추가)
    int countAttendedSessions(
            @Param("dogId") Long dogId,
            @Param("courseId") Long courseId
    );
}
