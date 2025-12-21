package com.mungtrainer.mtserver.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    /**
     * 주문 ID
     */
    private Long orderId;

    /**
     * 결제 ID
     */
    private Long paymentId;

    /**
     * 주문 상태
     */
    private String orderStatus;

    /**
     * 결제 상태
     */
    private String paymentStatus;

    /**
     * 총 금액
     */
    private Integer totalAmount;

    /**
     * 할인 금액
     */
    private Integer discountAmount;

    /**
     * 실제 결제 금액
     */
    private Integer paidAmount;

    /**
     * 결제 일시
     */
    private LocalDateTime paidAt;

    /**
     * 가맹점 주문번호
     */
    private String merchantUid;

    /**
     * 신청서 상태
     */
    private String applicationStatus;
}