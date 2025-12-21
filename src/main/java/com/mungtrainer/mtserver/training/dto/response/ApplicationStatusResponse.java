package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationStatusResponse {
    private Long applicationId;
    private String status; // APPLIED, WAITING, CANCELLED 등
}
