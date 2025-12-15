package com.mungtrainer.mtserver.training.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUploadRequest extends CourseBaseRequest{
  public CourseUploadRequest() {
    this.setStatus("SCHEDULED");
  }
}
