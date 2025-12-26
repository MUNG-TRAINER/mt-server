package com.mungtrainer.mtserver.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentLogResponse {
  private Long paymentId;
  private String orderName;
  private String status;
  private Integer amount;
  private String paymentKey;
  private LocalDateTime paymentDate;
}
