package com.mungtrainer.mtserver.order.controller;

import com.mungtrainer.mtserver.order.dto.request.PaymentRequest;
import com.mungtrainer.mtserver.order.dto.response.PaymentResponse;
import com.mungtrainer.mtserver.order.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 결제 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    /**
     * 결제 처리 API
     * POST /api/orders/payment
     *
     * @param request 결제 요청 정보
     * @return 결제 결과
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        log.info("결제 요청 수신 - applicationId: {}, userId: {}",
                request.getApplicationId(), request.getUserId());

        try {
            PaymentResponse response = orderPaymentService.processPayment(request);
            log.info("결제 성공 - orderId: {}, paymentId: {}",
                    response.getOrderId(), response.getPaymentId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("결제 실패 - 사유: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다.", e);
        }
    }
}