package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 주문 마스터 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderMaster extends BaseEntity {

    /**
     * 주문 ID (PK)
     */
    private Long orderId;

    /**
     * 가맹점 주문번호 (Unique)
     */
    private String merchantUid;

    /**
     * 사용자 ID (FK - user.user_id)
     */
    private Long userId;

    /**
     * 주문 상태 (READY_TO_PAY, PAYMENT_PENDING, PAID, CANCELLED)
     */
    private String orderStatus;

    /**
     * 총 금액
     */
    private Integer totalAmount;

    /**
     * 결제 금액
     */
    private Integer paidAmount;

    /**
     * 결제 일시
     */
    private LocalDateTime paidAt;

    /**
     * 주문 제목
     */
    private String orderName;

    /**
     *
     * @param status
     * @param paidAmount
     * @param paidAt
     */
    public void updatePaymentStatus(String status, Integer paidAmount, LocalDateTime paidAt) {
        this.orderStatus = status;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
    }
}
