package com.mungtrainer.mtserver.counseling.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequest {
    private String status;        // ACCEPT, REJECTED
    private String rejectReason;  // REJECTED일 때만 필요

}
