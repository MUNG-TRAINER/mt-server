package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponse {

    private Long applicationId;
    private Long sessionId;
    private Long dogId;
    private LocalDateTime appliedAt;
    private String status;
    private String rejectReason;
}
