package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {
    @NotNull(message = "dogId는 필수입니다.")
    private Long dogId;
}
