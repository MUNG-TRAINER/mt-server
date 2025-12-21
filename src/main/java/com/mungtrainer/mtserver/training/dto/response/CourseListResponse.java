package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CourseListResponse {
  private Long courseId;
    private Long trainerId;
  private String title;
  private String type;
  private String lessonForm;
  private String mainImage;
    private String location;
    private String tags;
    private String description;

    // session
    private Long sessionId;
    private Integer sessionNo;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionStatus;

  public void setMainImage(String mainImage) {
    this.mainImage = mainImage;
  }
}
