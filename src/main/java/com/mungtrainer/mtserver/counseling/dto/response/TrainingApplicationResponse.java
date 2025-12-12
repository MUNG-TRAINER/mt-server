package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class TrainingApplicationResponse {
    // 과정 정보
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String tags;
    private String type; // 일회성 / 다회차 구분

    // 세션 정보
    private Long sessionId;
    private LocalDate sessionDate;
    private LocalTime sessionStartTime;
    private LocalTime sessionEndTime;

//     회차 정보
    private Integer timesApplied; // tags 기준 신청 횟수

    // 출석 횟수 (ATTENDED)
    private Integer attendedCount;

    // 출석 상태
    private String attendanceStatus;
}
