package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ApplicationListViewResponse {
// 리스트 조회 전용

    // 신청 식별
    private Long applicationId;
    private Long sessionId;
    private Long dogId;

    // 신청 상태
    private String applicationStatus;
    private String rejectReason;

    // 코스 정보 (리스트 UI)
    private String tags;
    private String title;
    private String description;
    private String mainImage;
    private String location;

    // 세션 // ex: "2025-12-20 10:00 ~ 12:00"
    private String sessionSchedule;
    private String dogName;
}
