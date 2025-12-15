package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 환불 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Refund extends BaseEntity {

    /**
     * 환불 ID (PK)
     */
    private Long refundId;

    /**
     * 결제 ID (FK - payment.payment_id)
     */
    private Long paymentId;

    /**
     * 환불 금액
     */
    private Integer refundAmount;

    /**
     * 환불 상태 (REQUESTED, SUCCESS, FAILED)
     */
    private String refundStatus;

    /**
     * PG 환불 거래 ID
     */
    private String pgRefundTid;

    /**
     * 환불 사유
     */
    private String reason;

    /**
     * 요청 일시
     */
    private LocalDateTime requestedAt;

    /**
     * 환불 완료 일시
     */
    private LocalDateTime refundedAt;
}

