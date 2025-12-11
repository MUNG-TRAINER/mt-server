package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseUploadRequest {
  @Pattern(regexp = "^(ONCE|MULTI)$", message = "type은 ONCE 또는 MULTI만 가능합니다.")
  private String type;
  @Pattern(regexp = "^(WALK|GROUP|PRIVATE)$", message = "lessonForm은 WALK, GROUP, PRIVATE 중 하나여야 합니다.")
  private String lessonForm;

  private String title;
  private String description;
  private String status;
  private Boolean isFree;
  private String difficulty;
  private String location;
  private String schedule;
  private String refundPolicy;
  private String mainImage;
  private String detailImage;
  private String items;
  private String dogSize;
  private List<SessionUploadRequest>  sessionUploadRequests;
}
