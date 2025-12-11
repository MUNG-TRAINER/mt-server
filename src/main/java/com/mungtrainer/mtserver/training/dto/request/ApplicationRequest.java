package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ApplicationRequest {
    @NotNull(message = "sessionId는 필수입니다.")
    @Min(value = 1, message = "sessionId는 1 이상이어야 합니다.")
    private Long sessionId;
    @NotNull(message = "dogId는 필수입니다.")
    @Min(value = 1, message = "dogId는 1 이상이어야 합니다.")
    private Long dogId;
}
