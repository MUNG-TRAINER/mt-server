package com.mungtrainer.mtserver.counseling.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequestDTO {
    private String status;        // APPROVED, REJECTED
    private String rejectReason;  // REJECTED일 때만 필요
    private Long trainerId;       // updated_by

}
