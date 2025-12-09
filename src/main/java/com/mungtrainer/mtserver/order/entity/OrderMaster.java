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
     * 장바구니 ID (FK - wishlist.wishlist_id)
     */
    private Long wishlistId;

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
}

