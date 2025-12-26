package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PaymentPrepareRequest {
    private Integer amount;
    private List<Long> courseIds;
}
