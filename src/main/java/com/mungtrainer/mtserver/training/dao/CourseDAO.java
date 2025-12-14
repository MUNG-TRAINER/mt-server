package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.response.CourseListResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseDAO {
  // 조회
  List<CourseListResponse> findCourses(Long userId, List<String> statuses, List<String> types, List<String> lessonForms);
  TrainingCourse getCourseById(Long courseId);
  boolean isOwnerCourse(Long courseId, Long userId);
  boolean hasPaidApplications(Long courseId);
  boolean existsTags(String tags);

  // 삽입
  void insertCourse(TrainingCourse course);
  void insertSessions(List<TrainingSession> trainingSessions);

  // 수정
  void updateCourse(TrainingCourse course);

  // 삭제(상태 cancelled와 softDelete도 삭제로 포함)
  void cancelSessionsAndApplications(Long courseId, Long userId);
  void cancelCourse(Long courseId, Long userId);
  void softDeleteByApplication(Long courseId, Long userId);
  void softDeleteFeedbackAttachments(Long courseId, Long userId);
  void softDeleteBySession(Long courseId, Long userId);
  void deleteWishlistDetailDog(Long courseId);
  void deleteWishlistDetail(Long courseId);

}
