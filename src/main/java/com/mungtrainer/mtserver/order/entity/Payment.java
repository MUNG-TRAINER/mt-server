package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 결제 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends BaseEntity {

    /**
     * 결제 ID (PK)
     */
    private Long paymentId;

    /**
     * 주문 ID (FK - order_master.order_id)
     */
    private Long orderId;

    /**
     * 결제 방법
     */
    private String method;

    /**
     * 결제 금액
     */
    private Integer amount;

    /**
     * 결제 상태 (SUCCESS, FAILED, REFUNDED, CANCELLED)
     */
    private String paymentStatus;

    /**
     * 가맹점 주문번호 (Unique)
     */
    private String merchantUid;

    private String paymentKey;

    /**
     * 결제 일시
     */
    private LocalDateTime paidAt;

    public void updateStatus(String status) {
        this.paymentStatus = status;
    }
}
