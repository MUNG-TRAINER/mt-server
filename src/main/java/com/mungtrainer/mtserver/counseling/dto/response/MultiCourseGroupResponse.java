package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private Integer totalSessions; // 전체 회차 수
    private List<MultiSessionResponse> sessions; // 회차별 세션 정보

    private int attendedSessions;
    private double attendanceRate;

//    private Long sessionId;
//    private Integer sessionNo;
//    private LocalDate sessionDate;
//    private LocalTime startTime;
//    private LocalTime endTime;
//    private String locationDetail;
//    private String attendanceStatus;

}
