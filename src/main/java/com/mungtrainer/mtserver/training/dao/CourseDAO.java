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

//  삭제 부분
  void softDeleteSessions(Long courseId, Long userId);
  void softDeleteApplications(Long courseId, Long userId);
  void softDeleteWaiting(Long courseId, Long userId);
  void softDeleteAttendance(Long courseId, Long userId);
  void softDeleteSessionChange(Long courseId, Long userId);
  void softDeleteNotices(Long courseId, Long userId);
  void softDeleteWishlistDetail(Long courseId, Long userId);
  void softDeleteWishlistDetailDog(Long courseId, Long userId);
  void softDeleteFeedback(Long courseId, Long userId);
  void softDeleteFeedbackAttachment(Long courseId, Long userId);
  void softDeleteCourse(Long courseId, Long userId);
}
