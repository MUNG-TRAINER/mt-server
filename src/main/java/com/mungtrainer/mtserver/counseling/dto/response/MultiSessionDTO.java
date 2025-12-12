package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiSessionDTO {
    private Long sessionId;
    private Integer sessionNo;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationDetail;
    private String attendanceStatus; // 출석 상태
}
