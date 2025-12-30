package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class ApplicationListViewResponse {
// 리스트 조회 전용


    // 코스 정보 (리스트 UI)
    private Long courseId;
    private String tags;
    private String title;
    private String description;
    private String mainImage;
    private String location;
    private String lessonForm;
    private String type;
    private Long totalAmount;

    // 세션 // ex: "2025-12-20 10:00 ~ 12:00"
    private String sessionSchedule;

    private String rejectReason;
    private String dogName;
    private Long dogId;

    private List<ApplicationItem> applicationItems;

    @Getter
    @Builder
    public static class ApplicationItem {
        // 신청 식별
        private Long applicationId;

        // 신청 상태
        private String applicationStatus;
        private Long price;

        // 세션 정보
        private String sessionSchedule;
        private String rejectReason;

        // 대기 정보 (WAITING 상태일 때만 사용)
        private Boolean isWaiting;
        private Integer waitingOrder; // 대기 순번 (1부터 시작)
        private Boolean isPreApproved; // 미리 승인 여부
    }
}
