package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 훈련 세션 변경 이력 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingSessionChange extends BaseEntity {

    /**
     * 변경 ID (PK)
     */
    private Long changeId;

    /**
     * 세션 ID (FK - training_session.session_id)
     */
    private Long sessionId;

    /**
     * 변경 전 날짜
     */
    private LocalDate beforeDate;

    /**
     * 변경 전 시작 시간
     */
    private LocalTime beforeStartTime;

    /**
     * 변경 전 종료 시간
     */
    private LocalTime beforeEndTime;

    /**
     * 변경 후 날짜
     */
    private LocalDate afterDate;

    /**
     * 변경 후 시작 시간
     */
    private LocalTime afterStartTime;

    /**
     * 변경 후 종료 시간
     */
    private LocalTime afterEndTime;

    /**
     * 변경한 사용자 ID
     */
    private Long changedBy;

    /**
     * 변경 사유
     */
    private String reason;
}

