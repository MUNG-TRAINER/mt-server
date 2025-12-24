package com.mungtrainer.mtserver.order.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentApprovalRequest {
    private String paymentKey;
    private String merchantUid; // 주문번호 (가맹점 주문번호)
    private Integer amount;
}
