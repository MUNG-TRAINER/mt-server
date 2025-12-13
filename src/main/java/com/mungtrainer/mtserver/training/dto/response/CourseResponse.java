package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CourseResponse {
  private String status;
  private int code;
  private String message;
}
