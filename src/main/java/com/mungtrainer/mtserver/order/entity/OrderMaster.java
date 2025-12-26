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
     * 주문의 결제 상태와 결제 금액, 결제 일시 정보를 업데이트합니다.
     *
     * @param status     변경할 주문 결제 상태 (예: READY_TO_PAY, PAYMENT_PENDING, PAID, CANCELLED)
     * @param paidAmount 실제 결제된 금액
     * @param paidAt     결제가 완료된 일시
     */
    public void updatePaymentStatus(String status, Integer paidAmount, LocalDateTime paidAt) {
        this.orderStatus = status;
        this.paidAmount = paidAmount;
        this.paidAt = paidAt;
    }
}
