package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseDAO {
  void insertCourse(TrainingCourse course);
  void insertSessions(List<TrainingSession> trainingSessions);
  void updateCourse(TrainingCourse course);

  TrainingCourse getCourseById(Long courseId);
  boolean isOwnerCourse(Long courseId, Long userId);
  boolean hasPaidApplications(Long courseId);

  boolean existsTags(String tags);

  // 삭제 부분
  void cancelSessionsAndApplications(Long courseId, Long userId);
  void cancelCourse(Long courseId, Long userId);
  void softDeleteByApplication(Long courseId, Long userId);
  void softDeleteFeedbackAttachments(Long courseId, Long userId);
  void softDeleteBySession(Long courseId, Long userId);
  void deleteWishlistDetailDog(Long courseId);
  void deleteWishlistDetail(Long courseId);

}
