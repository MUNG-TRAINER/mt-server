package com.mungtrainer.mtserver.counseling.dto.response;

import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter @Builder
public class DogStatsResponse {

    private DogResponse dog;
    private List<CounselingResponse> counselings;
    private Stats stats;     // 통계 정보는 여기에만 포함됨
    private List<TrainingSessionDto> trainingApplications; // 단회차
    private List<MultiCourseCategoryResponse> multiCourses;  // 다회차

    @Getter @Setter @AllArgsConstructor
    public static class Stats {
        private Integer timesApplied;
        private Integer attendedCount;
    }

    @Getter @Setter @Builder
    public static class TrainingSessionDto {
        private Long courseId;
        private String courseTitle;
        private String courseDescription;
        private String tags;
        private String type;
        private String difficulty;  // 난이도 (BEGINNER, INTERMEDIATE, ADVANCED)
        private Long sessionId;
        private LocalDate sessionDate;
        private LocalTime sessionStartTime;
        private LocalTime sessionEndTime;
        private String attendanceStatus;  // 출석 상태 (ATTENDED, ABSENT, null)

    }

}

