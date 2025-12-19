package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 훈련 코스 신청 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingCourseApplication extends BaseEntity {

    /**
     * 신청 ID (PK)
     */
    private Long applicationId;

    /**
     * 세션 ID (FK - training_session.session_id)
     */
    private Long sessionId;

    /**
     * 반려견 ID (FK - dog.dog_id)
     */
    private Long dogId;

    /**
     * 신청 일시
     */
    private LocalDateTime appliedAt;

    /**
     * 상태 (APPLIED, PAID, CANCELLED, WAITING, REJECTED, ACCEPT, NO_SHOW, EXPIRED, REFUNDED, REFUND_REQUESTED)
     */
    @NotNull
    @Pattern(regexp = "APPLIED|PAID|CANCELLED|WAITING|REJECTED|ACCEPT|NO_SHOW|EXPIRED|REFUNDED|REFUND_REQUESTED|COUNSELING_REQUIRED",
            message = "유효하지 않은 상태입니다.")
    private String status;

    /**
     * 거절 사유
     */
    private String rejectReason;
}

