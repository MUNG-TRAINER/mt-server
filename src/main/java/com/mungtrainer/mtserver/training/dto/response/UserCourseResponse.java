package com.mungtrainer.mtserver.training.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class UserCourseResponse {
    // course
    private Long courseId;
    private String title;
    private String mainImage;
    private String lessonForm;
    private String difficulty;
    private String location;
    private String type;
    private String tags;
    private String description;

    // session
    private Long sessionId;
    private Integer sessionNo;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionStatus;

    // order
    private String orderStatus;

    // dog
    private String dogName;
}
