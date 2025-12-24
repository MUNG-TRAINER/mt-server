package com.mungtrainer.mtserver.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentApprovalResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method;
    private Integer totalAmount;
    private String status;
    private LocalDateTime approvedAt;
}
