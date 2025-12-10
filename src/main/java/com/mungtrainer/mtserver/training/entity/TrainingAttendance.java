package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 훈련 출석 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingAttendance extends BaseEntity {

    /**
     * 출석 ID (PK)
     */
    private Long attendanceId;

    /**
     * 신청 ID (FK - training_course_application.application_id)
     */
    private Long applicationId;

    /**
     * 상태 (ATTENDED, ABSENT, CANCELED)
     */
    private String status;

    /**
     * 체크인 시간
     */
    private LocalDateTime checkinTime;

    /**
     * 체크아웃 시간
     */
    private LocalDateTime checkoutTime;

    /**
     * 메모
     */
    private String memo;
}

