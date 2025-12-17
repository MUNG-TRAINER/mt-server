package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchItemDto;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingCourseDao {
    TrainingCourse findByCourseId(@Param("courseId")Long courseId);

    /**
     * 훈련과정 검색
     */
    List<CourseSearchItemDto> searchCourses(@Param("request") CourseSearchRequest request);

    /**
     * 검색 결과 전체 개수 조회
     */
    Integer countSearchResults(@Param("request") CourseSearchRequest request);
}
