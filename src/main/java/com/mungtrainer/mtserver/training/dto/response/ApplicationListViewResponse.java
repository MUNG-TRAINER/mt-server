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
    }
}
