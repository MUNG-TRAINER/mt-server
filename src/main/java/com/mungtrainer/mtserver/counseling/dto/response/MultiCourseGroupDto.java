package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiCourseGroupDto {
    private Long courseId;
    private String title;
    private String tags;
    private String description;
    private String location;
    private String type;
    private String difficulty;
    private String mainImage;

    private Integer totalSessions; // 전체 회차 수
    private List<MultiSessionDto> sessions; // 회차별 세션 정보

    private int attendedSessions;
    private double attendanceRate;

}
