package com.mungtrainer.mtserver.order.dto.request;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
public class PaymentPrepareRequest {
    private List<PaymentRequestItem> paymentRequestItems;

    @Getter
    @NoArgsConstructor
    public static class PaymentRequestItem {
        private Long courseId;
        private Long applicationId;
    }
}
