package com.mungtrainer.mtserver.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentCancelResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private Integer canceledAmount;
    private LocalDateTime canceledAt;
    private String cancelReason;
}
