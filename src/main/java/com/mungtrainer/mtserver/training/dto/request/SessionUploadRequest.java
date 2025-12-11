package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SessionUploadRequest {

  @NotBlank
  @Pattern(regexp = "SCHEDULED|CANCELED|DONE")
  private String status;

  private Integer sessionNo;
  private LocalDate sessionDate;
  private LocalTime startTime;
  private LocalTime endTime;
  private String locationDetail;
  private Integer maxStudents;
  private String content;
  private Integer price;
}
