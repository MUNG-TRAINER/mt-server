package com.mungtrainer.mtserver.training.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class UserCourseSessionResponse {
    private Long sessionId;
    private Integer sessionNo;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionStatus;
    private String applicationStatus;
}
