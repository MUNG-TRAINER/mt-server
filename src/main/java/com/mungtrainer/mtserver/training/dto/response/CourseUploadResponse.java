package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CourseUploadResponse {
  private String status;
  private int code;
  private String message;
}
