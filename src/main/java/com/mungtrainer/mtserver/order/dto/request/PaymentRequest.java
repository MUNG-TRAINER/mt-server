package com.mungtrainer.mtserver.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "신청서 ID는 필수입니다.")
    private Long applicationId;

    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    /**
     * 결제 방법 (나중에 오픈뱅킹 연동 시 사용)
     * 예: CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT 등
     */
    private String paymentMethod;

    /**
     * 할인 금액 (선택사항)
     */
    @Min(value = 0, message = "할인 금액은 0 이상이어야 합니다.")
    private Integer discountAmount;
}