package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchItemDto;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TrainingCourseDao {
    TrainingCourse findByCourseId(@Param("courseId")Long courseId);

    /**
     * 훈련과정 검색 (무한 스크롤 - 커서 기반)
     */
    List<CourseSearchItemDto> searchCourses(@Param("request") CourseSearchRequest request);

    /**
     * 특정 날짜의 코스 목록 조회
     */
    List<CourseSearchItemDto> findCoursesByDate(
            @Param("date") LocalDate date,
            @Param("trainerId") Long trainerId,
            @Param("keyword") String keyword,
            @Param("lessonForm") String lessonForm);
}
