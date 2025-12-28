package com.mungtrainer.mtserver.order.controller;


import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.order.dto.request.PaymentApprovalRequest;
import com.mungtrainer.mtserver.order.dto.request.PaymentCancelRequest;
import com.mungtrainer.mtserver.order.dto.request.PaymentPrepareRequest;
import com.mungtrainer.mtserver.order.dto.response.PaymentApprovalResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentCancelResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentLogResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentPrepareResponse;
import com.mungtrainer.mtserver.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * 1단계: 결제 준비 (주문 생성)
   * 프론트엔드에서 "결제하기" 클릭 시 호출
   */
  @PostMapping("/prepare")
  public ResponseEntity<PaymentPrepareResponse> preparePayment(
      @RequestBody PaymentPrepareRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
      log.info("결제 준비 요청 - userId: {}", userDetails.getUserId());

      PaymentPrepareResponse response = paymentService.preparePayment(request, userDetails.getUserId());

      log.info("결제 준비 완료 - merchantUid: {}", response.getMerchantUid());
      return ResponseEntity.ok(response);
  }

  /**
   * 결제 승인 API
   * 프론트엔드에서 토스 결제 인증 완료 후 호출
   */
  @PostMapping("/confirm")
  public ResponseEntity<PaymentApprovalResponse> confirmPayment(
          @RequestBody PaymentApprovalRequest request
  ) {
      log.info("결제 승인 요청 - merchantUid: {}, paymentKey: {}, amount: {}",
              request.getMerchantUid(), request.getPaymentKey(), request.getAmount());

      PaymentApprovalResponse response = paymentService.approvePayment(request);

      log.info("결제 승인 완료 - merchantUid: {}", request.getMerchantUid());
      return ResponseEntity.ok(response);
  }

  /**
   * 결제 취소 API
   */
  @PostMapping("/cancel")
  public ResponseEntity<PaymentCancelResponse> cancelPayment(
          @RequestBody PaymentCancelRequest request,
          @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
      log.info("결제 취소 요청 - paymentKey: {}, cancelReason: {}",
              request.getPaymentKey(), request.getCancelReason());

      PaymentCancelResponse response = paymentService.cancelPayment(request, userDetails.getUserId());

      log.info("결제 취소 완료 - paymentKey: {}", request.getPaymentKey());
      return ResponseEntity.ok(response);
  }

  /**
   * 현재 인증된 사용자의 결제 이력 로그 목록을 조회하여 반환합니다.
   */
  @GetMapping("/logs")
  public ResponseEntity<List<PaymentLogResponse>> getPaymentLogs(
      @AuthenticationPrincipal CustomUserDetails userDetails){
    List<PaymentLogResponse> response = paymentService.getPaymentLogs(userDetails.getUserId());
    return ResponseEntity.ok(response);
  }
}
