package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionResponse {

    private Long sessionId;
    private Integer sessionNo;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationDetail;
    private String status;
    private Integer maxStudents;
    private Integer currentStudents; // 현재 신청 인원
    private String content;
    private Integer price;
}