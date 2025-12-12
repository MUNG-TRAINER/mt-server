package com.mungtrainer.mtserver.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    /**
     * 훈련과정 신청서 ID
     */
    private Long applicationId;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 결제 방법 (나중에 오픈뱅킹 연동 시 사용)
     * 예: CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT 등
     */
    private String paymentMethod;

    /**
     * 할인 금액 (선택사항)
     */
    private Integer discountAmount;
}