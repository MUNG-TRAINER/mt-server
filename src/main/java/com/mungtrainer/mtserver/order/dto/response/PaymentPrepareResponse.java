package com.mungtrainer.mtserver.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentPrepareResponse {
    private String merchantUid;
    private Integer amount;
    private boolean isCompleted;
    private String orderName;
    private Long trainerId;
}
