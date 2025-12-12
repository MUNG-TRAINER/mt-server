package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.training.dao.CourseDAO;
import com.mungtrainer.mtserver.training.dto.request.*;
import com.mungtrainer.mtserver.training.dto.response.CourseResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseDAO courseDAO;
  private final S3Service s3Service;

  @Transactional
  public CourseResponse createCourse(CourseUploadRequest req, Long userId) {
    String tags = generateUUID();

    TrainingCourse course = buildCourse(CourseBaseRequest.from(req), userId, tags);

    // DB insert
    courseDAO.insertCourse(course);

    Long courseId = course.getCourseId();
    List<TrainingSession> sessions = buildSessions(req.getSessionUploadRequests(),courseId, userId);

    courseDAO.insertSessions(sessions);

    return CourseResponse.builder()
                         .status("Success")
                         .code(201)
                         .message("훈련 과정 업로드 완료")
                         .build();
  }

  @Transactional
  public CourseResponse courseReupload(CourseReuploadRequest req, Long userId) {
    validateUser(userId, req.getTrainerId());

    String tags = req.getTags();
    TrainingCourse course = buildCourse(CourseBaseRequest.from(req), userId, tags);

    // DB insert
    courseDAO.insertCourse(course);

    Long courseId = course.getCourseId();
    List<TrainingSession> sessions = buildSessions(req.getSessionUploadRequests(), courseId, userId );

    courseDAO.insertSessions(sessions);

    return CourseResponse.builder()
                         .status("Success")
                         .code(201)
                         .message("훈련 과정 재업로드 완료")
                         .build();
  }

  @Transactional
  public CourseResponse updateCourse(CourseUpdateRequest req, Long courseId, Long userId) {

    validateOwner(userId, courseId);
    TrainingCourse course = courseDAO.getCourseById(courseId);

    String oldMainImage = course.getMainImage();
    String oldDetailImage = course.getDetailImage();
    String newMainImage = req.getMainImage();
    String newDetailImage = req.getDetailImage();

    // 새 이미지가 있고, 기존 이미지와 다른 경우에만 삭제
    deleteIfChanged(oldMainImage, newMainImage);
    deleteIfChanged(oldDetailImage, newDetailImage);


    course = TrainingCourse.builder()
                           .trainerId(userId)
                           .tags(req.getTags())
                           .type(req.getType())
                           .lessonForm(req.getLessonForm())
                           .title(req.getTitle())
                           .description(req.getDescription())
                           .status(req.getStatus())
                           .isFree(req.getIsFree())
                           .location(req.getLocation())
                           .mainImage(req.getMainImage())
                           .detailImage(req.getDetailImage())
                           .refundPolicy(req.getRefundPolicy())
                           .schedule(req.getSchedule())
                           .dogSize(req.getDogSize())
                           .items(req.getItems())
                           .difficulty(req.getDifficulty())
                           .updatedBy(userId)
                           .updatedAt(LocalDateTime.now())
                           .build();

    courseDAO.updateCourse(course);

    return CourseResponse.builder()
                         .status("Success")
                         .code(200)
                         .message("훈련 과정 수정 완료")
                         .build();
  }

  @Transactional
  public void deleteCourse(Long courseId, Long userId) {

    validateOwner(userId, courseId);
    TrainingCourse course = courseDAO.getCourseById(courseId);

    if (courseDAO.hasPaidApplications(courseId)) {
      throw new CustomException(ErrorCode.COURSE_HAS_PAID_APPLICATIONS);
    }

    courseDAO.softDeleteSessions(courseId, userId);
    courseDAO.softDeleteApplications(courseId, userId);
    courseDAO.softDeleteWaiting(courseId, userId);
    courseDAO.softDeleteAttendance(courseId, userId);
    courseDAO.softDeleteSessionChange(courseId, userId);
    courseDAO.softDeleteNotices(courseId, userId);
    courseDAO.softDeleteWishlistDetail(courseId, userId);
    courseDAO.softDeleteWishlistDetailDog(courseId, userId);
    courseDAO.softDeleteFeedback(courseId, userId);
    courseDAO.softDeleteFeedbackAttachment(courseId, userId);
    courseDAO.softDeleteCourse(courseId, userId);

    s3Service.deleteFile(course.getMainImage());
    s3Service.deleteFile(course.getDetailImage());
  }

  private void validateOwner(Long userId, Long courseId) {
    if ( !courseDAO.isOwnerCourse(courseId, userId) ) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_RESOURCE_ACCESS);
    }
  }

  private void validateUser(Long userId, Long trainerId) {
    if ( !trainerId.equals(userId) ) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_RESOURCE_ACCESS);
    }
  }

  private void deleteIfChanged(String oldKey, String newKey) {
    if (newKey != null
        && !newKey.isBlank()
        && oldKey != null
        && !oldKey.isBlank()
        && !newKey.equals(oldKey)) {

      s3Service.deleteFile(oldKey);
    }
  }

  private TrainingCourse buildCourse(CourseBaseRequest req, Long userId, String tags) {

    return TrainingCourse.builder()
                         .trainerId(userId)
                         .tags(tags)
                         .title(req.getTitle())
                         .description(req.getDescription())
                         .type(req.getType())
                         .lessonForm(req.getLessonForm())
                         .status(req.getStatus())
                         .isFree(req.getIsFree())
                         .difficulty(req.getDifficulty())
                         .location(req.getLocation())
                         .schedule(req.getSchedule())
                         .refundPolicy(req.getRefundPolicy())
                         .mainImage(req.getMainImage())
                         .detailImage(req.getDetailImage())
                         .items(req.getItems())
                         .dogSize(req.getDogSize())
                         .createdBy(userId)
                         .updatedBy(userId)
                         .build();
  }

  private List<TrainingSession> buildSessions(List<SessionUploadRequest> sessionReqs,
                                              Long courseId, Long userId) {

    List<TrainingSession> sessions = new ArrayList<>();

    for (SessionUploadRequest s : sessionReqs) {
      sessions.add(TrainingSession.builder()
                                  .courseId(courseId)
                                  .sessionNo(s.getSessionNo())
                                  .sessionDate(s.getSessionDate())
                                  .startTime(s.getStartTime())
                                  .endTime(s.getEndTime())
                                  .locationDetail(s.getLocationDetail())
                                  .status(s.getStatus())
                                  .maxStudents(s.getMaxStudents())
                                  .content(s.getContent())
                                  .price(s.getPrice())
                                  .createdBy(userId)
                                  .updatedBy(userId)
                                  .build());
    }

    return sessions;
  }

  private String generateUUID() {
    String tags = null;
    final int MAX_RETRY = 20;
    int retry = 0;
    while (retry < MAX_RETRY) {
      tags = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
      if (!courseDAO.existsTags(tags)) {
        break;
      }
      retry++;
    }
    if (retry == MAX_RETRY) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    return tags;
  }
}
