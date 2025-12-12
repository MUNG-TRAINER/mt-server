package com.mungtrainer.mtserver.order.service;


import com.mungtrainer.mtserver.dog.dao.DogMapper;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.entity.Dog;
import com.mungtrainer.mtserver.order.dto.response.PaymentResponse;
import com.mungtrainer.mtserver.order.dto.request.PaymentRequest;
import com.mungtrainer.mtserver.order.entity.OrderMaster;
import com.mungtrainer.mtserver.order.entity.OrderItem;
import com.mungtrainer.mtserver.order.dao.OrderDAO;
import com.mungtrainer.mtserver.order.entity.Payment;
import com.mungtrainer.mtserver.order.entity.PaymentLog;
import com.mungtrainer.mtserver.order.dao.PaymentDAO;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import com.mungtrainer.mtserver.training.dao.TrainingCourseApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    // 상수 정의
    private static final String APPLICATION_STATUS_APPLIED = "APPLIED";
    private static final String APPLICATION_STATUS_PAID = "PAID";
    private static final String ORDER_STATUS_PAYMENT_PENDING = "PAYMENT_PENDING";
    private static final String ORDER_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    private static final String PAYMENT_LOG_STATUS_REQUESTED = "REQUESTED";
    private static final String PAYMENT_LOG_STATUS_SUCCESS = "SUCCESS";

    private final TrainingCourseApplicationDAO applicationMapper;
    private final TrainingSessionDAO sessionMapper;
    private final OrderDAO orderMapper;
    private final PaymentDAO paymentMapper;
    private final DogMapper dogMapper;

    /**
     * 결제 처리 메인 메서드
     * - 승인된 신청서 검증
     * - 주문 생성
     * - 결제 처리
     * - 상태 업데이트
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("===== 결제 처리 시작 =====");
        log.info("신청서 ID: {}, 사용자 ID: {}", request.getApplicationId(), request.getUserId());

        // 1. 신청서 검증 및 조회
        TrainingCourseApplication application = validateAndGetApplication(request);
        log.info("신청서 검증 완료 - 반려견 ID: {}, 세션 ID: {}", application.getDogId(), application.getSessionId());

        // 2. 세션 정보 조회 (가격 정보)
        TrainingSession session = getTrainingSession(application.getSessionId());
        log.info("세션 조회 완료 - 가격: {}원", session.getPrice());

        // 3. 주문 생성
        OrderMaster order = createOrder(request, session);
        log.info("주문 생성 완료 - 주문 ID: {}, 총액: {}원, 결제액: {}원",
                order.getOrderId(), order.getTotalAmount(), order.getPaidAmount());

        // 4. 주문 항목 생성
        OrderItem orderItem = createOrderItem(order.getOrderId(), application.getApplicationId(), session.getPrice());
        log.info("주문 항목 생성 완료 - 주문 항목 ID: {}", orderItem.getOrderItemId());

        // 5. 결제 처리
        Payment payment = processPaymentTransaction(order, request);
        log.info("결제 처리 완료 - 결제 ID: {}, 가맹점 주문번호: {}", payment.getPaymentId(), payment.getMerchantUid());

        // 6. 결제 요청 로그 기록
        createPaymentLog(payment, PAYMENT_LOG_STATUS_REQUESTED, "결제 요청");

        // 7. 주문 상태 업데이트 (PAYMENT_PENDING → PAID)
        updateOrderStatus(order.getOrderId(), ORDER_STATUS_PAID);
        log.info("주문 상태 업데이트 완료 - 상태: {}", ORDER_STATUS_PAID);

        // 8. 신청서 상태 업데이트 (APPLIED → PAID)
        updateApplicationStatus(application.getApplicationId(), APPLICATION_STATUS_PAID);
        log.info("신청서 상태 업데이트 완료 - 상태: {}", APPLICATION_STATUS_PAID);

        // 9. 결제 완료 로그 기록
        createPaymentLog(payment, PAYMENT_LOG_STATUS_SUCCESS, "결제 완료");

        log.info("===== 결제 처리 완료 =====");

        // 10. 응답 생성
        return buildPaymentResponse(order, payment, application);
    }

    /**
     * 신청서 검증 및 조회
     */
    private TrainingCourseApplication validateAndGetApplication(PaymentRequest request) {
        TrainingCourseApplication application = applicationMapper.findById(request.getApplicationId());

        if (application == null) {
            throw new IllegalArgumentException("존재하지 않는 신청서입니다. applicationId: " + request.getApplicationId());
        }

        // 신청서 상태 확인 (APPLIED 상태만 결제 가능)
        if (!APPLICATION_STATUS_APPLIED.equals(application.getStatus())) {
            throw new IllegalStateException(
                    String.format("신청 상태(APPLIED)인 경우에만 결제할 수 있습니다. 현재 상태: %s", application.getStatus())
            );
        }

        // 반려견 소유자 검증 (한 번에 처리)
        DogResponse dog = dogMapper.selectDogByIdAndUserId(application.getDogId(), request.getUserId());

        if (dog == null) {
          throw new IllegalArgumentException("존재하지 않는 반려견이거나 해당 신청서에 대한 권한이 없습니다.");
        }

        return application;
    }

    /**
     * 세션 정보 조회
     */
    private TrainingSession getTrainingSession(Long sessionId) {
        TrainingSession session = sessionMapper.findById(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        if (session.getPrice() == null || session.getPrice() <= 0) {
            throw new IllegalStateException("유효하지 않은 가격 정보입니다. price: " + session.getPrice());
        }

        return session;
    }

    /**
     * 주문 생성
     */
    private OrderMaster createOrder(PaymentRequest request, TrainingSession session) {
        Integer totalAmount = session.getPrice();
        Integer discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0;
        Integer paidAmount = totalAmount - discountAmount;

        if (paidAmount < 0) {
            throw new IllegalArgumentException("할인 금액이 총 금액보다 클 수 없습니다.");
        }

        if (paidAmount == 0) {
            throw new IllegalArgumentException("결제 금액은 0원보다 커야 합니다.");
        }

        OrderMaster order = OrderMaster.builder()
                .wishlistId(null)  // 직접 결제이므로 null
                .userId(request.getUserId())
                .orderStatus(ORDER_STATUS_PAYMENT_PENDING)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .build();

        orderMapper.insertOrder(order);
        return order;
    }

    /**
     * 주문 항목 생성
     */
    private OrderItem createOrderItem(Long orderId, Long applicationId, Integer price) {
        OrderItem orderItem = OrderItem.builder()
                .orderId(orderId)
                .applicationId(applicationId)
                .price(price)
                .build();

        orderMapper.insertOrderItem(orderItem);
        return orderItem;
    }

    /**
     * 결제 트랜잭션 처리
     * TODO: 나중에 오픈뱅킹 API 연동 시 실제 PG사 통신 로직 추가
     */
    private Payment processPaymentTransaction(OrderMaster order, PaymentRequest request) {
        // 가맹점 주문번호 생성 (Unique)
        String merchantUid = generateMerchantUid();

        // TODO: 실제 PG사 API 호출
        // - 결제 승인 요청
        // - PG 거래 ID 받기
        // - 결제 실패 시 예외 처리

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .method(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .amount(order.getPaidAmount())
                .paymentStatus(PAYMENT_STATUS_SUCCESS)
                .merchantUid(merchantUid)
                .paidAt(LocalDateTime.now())
                .build();

        paymentMapper.insertPayment(payment);
        return payment;
    }

    /**
     * 주문 상태 업데이트
     */
    private void updateOrderStatus(Long orderId, String status) {
        OrderMaster order = OrderMaster.builder()
                .orderId(orderId)
                .orderStatus(status)
                .paidAt(LocalDateTime.now())
                .build();

        orderMapper.updateOrderStatus(order);
    }

    /**
     * 신청서 상태 업데이트
     */
    private void updateApplicationStatus(Long applicationId, String status) {
        TrainingCourseApplication application = TrainingCourseApplication.builder()
                .applicationId(applicationId)
                .status(status)
                .build();

        applicationMapper.updateStatus(application);
    }

    /**
     * 결제 로그 생성
     */
    private void createPaymentLog(Payment payment, String status, String reason) {
        PaymentLog paymentLog = PaymentLog.builder()
                .paymentId(payment.getPaymentId())
                .status(status)
                .amount(payment.getAmount())
                .merchantUid(payment.getMerchantUid())
                .pgTid(null)  // 나중에 PG사 연동 시 사용
                .failureReason(status.equals(PAYMENT_LOG_STATUS_SUCCESS) ? null : reason)
                .build();

        paymentMapper.insertPaymentLog(paymentLog);
    }

    /**
     * 가맹점 주문번호 생성
     * 형식: ORD_yyyyMMddHHmmss_UUID
     */
    private String generateMerchantUid() {
      String merchantUid;
      int maxRetries = 3;
      int retry = 0;

      do {
        String timestamp = LocalDateTime.now()
            .toString()
            .replaceAll("[-:T.]", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        merchantUid = String.format("ORD_%s_%s", timestamp, uuid);

        // TODO: DB에 중복 체크 (옵션)
        // if (paymentMapper.existsByMerchantUid(merchantUid)) continue;

        break;
      } while (retry++ < maxRetries);

      return merchantUid;
    }

    /**
     * 결제 응답 DTO 생성
     */
    private PaymentResponse buildPaymentResponse(OrderMaster order, Payment payment, TrainingCourseApplication application) {
        Integer discountAmount = order.getTotalAmount() - order.getPaidAmount();

        return PaymentResponse.builder()
                .orderId(order.getOrderId())
                .paymentId(payment.getPaymentId())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(payment.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(discountAmount)
                .paidAmount(order.getPaidAmount())
                .paidAt(payment.getPaidAt())
                .merchantUid(payment.getMerchantUid())
                .applicationStatus(APPLICATION_STATUS_PAID)
                .build();
    }
}