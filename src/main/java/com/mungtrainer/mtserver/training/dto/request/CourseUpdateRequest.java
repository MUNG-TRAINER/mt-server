package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateRequest extends CourseBaseRequest{
    @NotNull
    private Long trainerId;
    @NotBlank
    private String tags;
}