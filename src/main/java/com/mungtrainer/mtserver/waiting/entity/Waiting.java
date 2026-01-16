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
     * 대기 상태
     * - WAITING: 대기중
     * - PROMOTED: 자동 승격됨 (정원 발생 시)
     * - CANCELLED: 취소됨
     * - EXPIRED: 만료됨
     *
     * 참고: 승격 시 training_course_application.status도 ACCEPT로 변경됨
     */
    private String status;

    /**
     * 미리 승인 여부 (훈련사가 대기 중인 신청을 미리 승인한 경우 true)
     * 자동 승격 시 이 값이 true이면 바로 ACCEPT 상태로 전환
     */
    private Boolean isApproved;
}
