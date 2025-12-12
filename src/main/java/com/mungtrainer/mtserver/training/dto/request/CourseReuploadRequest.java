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
public class CourseReuploadRequest extends CourseBaseRequest{

  @NotNull
  private Long trainerId;
  @NotBlank
  private String tags;

}
