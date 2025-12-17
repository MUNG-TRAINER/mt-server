package com.mungtrainer.mtserver.training.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainingCourseResponse {

    private Long trainerId;
    private String tags;
    private String title;
    private String description;
    private String type;
    private String status;
    private Boolean isFree;
    private String difficulty;
    private String location;
    private String schedule;
    private String refundPolicy;
    private String mainImage;
    private String mainImageKey;
    private String detailImage;
    private String detailImageKey;
    private String items;
    private String dogSize;
}
