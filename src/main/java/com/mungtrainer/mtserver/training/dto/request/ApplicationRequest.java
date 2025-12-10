package com.mungtrainer.mtserver.training.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicationRequest {
    private Long sessionId;
    private Long dogId;
}
