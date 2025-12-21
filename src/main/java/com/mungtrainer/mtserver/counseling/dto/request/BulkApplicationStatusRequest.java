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
    private String status; // ACCEPT, REJECTED
    private String rejectReason; // 거절 시 필수
}

