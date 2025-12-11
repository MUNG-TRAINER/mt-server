package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.training.dao.CourseDAO;
import com.mungtrainer.mtserver.training.dto.request.CourseUploadRequest;
import com.mungtrainer.mtserver.training.dto.request.SessionUploadRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseUploadResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
  private final CourseDAO courseDAO;

  public CourseUploadResponse createCourse(CourseUploadRequest req, Long userId) {
    String tags;
    do{
      tags = UUID.randomUUID().toString().replace("-", "").substring(0, 50);
    }while (courseDAO.existsTags(tags));
    TrainingCourse course = TrainingCourse.builder()
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

    // DB insert
    courseDAO.insertCourse(course);

    Long courseId = course.getCourseId();
    List<TrainingSession> sessions = new ArrayList<>();
    for (SessionUploadRequest s : req.getSessionUploadRequests()) {
      sessions.add(TrainingSession.builder()
                                               .courseId(courseId)
                                               .sessionNo(s.getSessionNo())
                                               .sessionDate(s.getSessionDate())
                                               .startTime(s.getStartTime())
                                               .endTime(s.getEndTime())
                                               .locationDetail(s.getLocationDetail())
                                               .status(s.getStatus())   // SCHEDULED | CANCELED | DONE
                                               .maxStudents(s.getMaxStudents())
                                               .content(s.getContent())
                                               .price(s.getPrice())
                                               .createdBy(userId)
                                               .updatedBy(userId)
                                               .build());
      courseDAO.insertSessions(sessions);
    }

    return CourseUploadResponse.builder()
                               .status("Success")
                               .code(201)
                               .message("훈련 과정 업로드 완료")
                               .build();
  }

  public CourseUploadResponse createSession(SessionUploadRequest req
                                           ,Long userId, Long courseId) {

    if ( courseDAO.getCourseById(courseId) == null ) {
      throw new CustomException(ErrorCode.INVALID_COURSE_ID);
    }

    TrainingSession session = TrainingSession.builder()
                                             .courseId(courseId)
                                             .sessionNo(req.getSessionNo())
                                             .sessionDate(req.getSessionDate())
                                             .startTime(req.getStartTime())
                                             .endTime(req.getEndTime())
                                             .locationDetail(req.getLocationDetail())
                                             .status(req.getStatus())   // SCHEDULED | CANCELED | DONE
                                             .maxStudents(req.getMaxStudents())
                                             .content(req.getContent())
                                             .price(req.getPrice())
                                             .createdBy(userId)
                                             .updatedBy(userId)
                                             .build();
    courseDAO.insertSession(session);

    return CourseUploadResponse.builder()
                               .status("Success")
                               .code(201)
                               .message("훈련 과정 업로드 완료")
                               .build();
  }
}
