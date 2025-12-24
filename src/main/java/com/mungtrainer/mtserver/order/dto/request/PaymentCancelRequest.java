package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCancelRequest {
    private String paymentKey;      // 결제 키
    private String cancelReason;    // 취소 사유
    private Integer cancelAmount;   // 취소 금액 (부분취소 시 사용, null이면 전액취소)
}
