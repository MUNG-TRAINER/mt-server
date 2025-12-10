package com.mungtrainer.mtserver.waiting.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 대기열 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Waiting extends BaseEntity {

    /**
     * 신청 ID (PK, FK - training_course_application.application_id)
     */
    private Long applicationId;

    /**
     * 상태 (WAITING, READY, ENTERED, CANCELLED, EXPIRED)
     */
    private String status;
}

