package com.mungtrainer.mtserver.counseling.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 코스별 일괄 승인/거절 요청 DTO
 */
@Getter
@Setter
public class BulkApplicationStatusRequest {
    private Long courseId; // 코스 ID
    private Long dogId; // 반려견 ID
    private String status; // ACCEPT, REJECTED
    private String rejectReason; // 거절 시 필수
    private List<Long> applicationIds; // 직접 applicationId 목록을 받을 수도 있음 (optional)
}

