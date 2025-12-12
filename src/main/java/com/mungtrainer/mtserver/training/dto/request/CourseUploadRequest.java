package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
  @NotNull(message = "sessionUploadRequests는 필수입니다.")
  @Size(min = 1, message = "최소 1개 이상의 세션 정보가 필요합니다.")
  private List<SessionUploadRequest>  sessionUploadRequests;
  @NotBlank
  private String title;
  @NotBlank
  private String description;
  @NotBlank
  private String status;
  @NotNull
  private Boolean isFree;
  @NotBlank
  private String location;
  @NotBlank
  private String mainImage;

  private String refundPolicy;
  private String schedule;
  private String dogSize;
  private String items;
  private String detailImage;
  private String difficulty;
}
