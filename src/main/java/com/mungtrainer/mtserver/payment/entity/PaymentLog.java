package com.mungtrainer.mtserver.payment.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 결제 로그 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentLog extends BaseEntity {

    /**
     * 로그 ID (PK)
     */
    private Long logId;

    /**
     * 결제 ID (FK - payment.payment_id)
     */
    private Long paymentId;

    /**
     * 상태 (REQUESTED, SUCCESS, FAILED)
     */
    private String status;

    /**
     * 금액
     */
    private Integer amount;

    /**
     * PG 거래 ID
     */
    private String pgTid;

    /**
     * 가맹점 주문번호
     */
    private String merchantUid;

    /**
     * 실패 사유
     */
    private String failureReason;
}

