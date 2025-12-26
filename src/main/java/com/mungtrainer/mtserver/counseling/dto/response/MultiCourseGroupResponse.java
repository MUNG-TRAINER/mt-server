package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiCourseGroupResponse {
    private Long courseId;
    private String title;
    private String tags;
    private String description;
    private String location;
    private String type;
    private String difficulty;
    private String mainImage;

    // 그룹화 정보
    private Integer enrollmentCount;  // 수강 횟수

    private Integer totalSessions; // 전체 회차 수
    private List<MultiSessionResponse> sessions; // 회차별 세션 정보

    private int attendedSessions;
    private double attendanceRate;

    // 수강 이력
    private List<EnrollmentHistory> enrollmentHistory;

    // 내부 클래스: 개별 수강 이력
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentHistory {
        private Integer enrollmentNumber;  // 몇 차 수강
        private Long courseId;
        private String title;  // 과정명 (미세한 차이)
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalSessions;
        private Integer attendedSessions;
        private Double attendanceRate;
        private List<MultiSessionResponse> sessions;
    }
}
