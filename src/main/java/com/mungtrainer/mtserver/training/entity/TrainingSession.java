package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 훈련 세션 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingSession extends BaseEntity {

    /**
     * 세션 ID (PK)
     */
    private Long sessionId;

    /**
     * 코스 ID (FK - training_course.course_id)
     */
    private Long courseId;

    /**
     * 세션 번호
     */
    private Integer sessionNo;

    /**
     * 세션 날짜
     */
    private LocalDate sessionDate;

    /**
     * 시작 시간
     */
    private LocalTime startTime;

    /**
     * 종료 시간
     */
    private LocalTime endTime;

    /**
     * 위치 상세
     */
    private String locationDetail;

    /**
     * 상태 (SCHEDULED, CANCELED, DONE)
     */
    private String status;

    /**
     * 최대 수강생 수
     */
    private Integer maxStudents;

    /**
     * 내용
     */
    private String content;

    /**
     * 가격
     */
    private Integer price;
}

