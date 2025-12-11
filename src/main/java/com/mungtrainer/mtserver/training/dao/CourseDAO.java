package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseDAO {
  int insertCourse(TrainingCourse course);
  int insertSession(TrainingSession trainingSession);
  int insertSessions(List<TrainingSession> trainingSessions);

  TrainingCourse getCourseById(Long courseId);

  boolean existsTags(String tags);
}
