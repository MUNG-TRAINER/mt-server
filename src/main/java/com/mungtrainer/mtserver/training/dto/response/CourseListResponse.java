package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseListResponse {
  private Long courseId;
  private Long trainerId;
  private String title;
  private String type;
  private String status;
  private String lessonForm;
  private Boolean isFree;
  private String difficulty;
  private String schedule;
  private String mainImage;

  public void setMainImage(String mainImage) {
    this.mainImage = mainImage;
  }
}
