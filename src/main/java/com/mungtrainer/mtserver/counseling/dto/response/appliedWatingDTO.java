package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class appliedWatingDTO {
    private String dogName;
    private String ownerName;
    private String courseTitle;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
