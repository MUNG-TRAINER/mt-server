package com.mungtrainer.mtserver.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.order.dao.OrderDAO;
import com.mungtrainer.mtserver.order.dao.PaymentDAO;
import com.mungtrainer.mtserver.order.dto.request.PaymentApprovalRequest;
import com.mungtrainer.mtserver.order.dto.request.PaymentCancelRequest;
import com.mungtrainer.mtserver.order.dto.request.PaymentPrepareRequest;
import com.mungtrainer.mtserver.order.dto.response.PaymentApprovalResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentCancelResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentLogResponse;
import com.mungtrainer.mtserver.order.dto.response.PaymentPrepareResponse;
import com.mungtrainer.mtserver.order.entity.OrderItem;
import com.mungtrainer.mtserver.order.entity.OrderMaster;
import com.mungtrainer.mtserver.order.entity.Payment;
import com.mungtrainer.mtserver.order.entity.PaymentLog;
import com.mungtrainer.mtserver.training.dao.CourseDAO;
import com.mungtrainer.mtserver.training.dao.TrainingCourseApplicationDAO;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import com.mungtrainer.mtserver.user.dao.UserDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderDAO orderDAO;
    private final PaymentDAO paymentDAO;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;
    private final TrainingCourseApplicationDAO trainingCourseApplicationDAO;

  @Value("${toss.secret-key}")
    private String secretKey;

    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/%s/cancel";

    private static final String ORDER_STATUS_READY_TO_PAY = "READY_TO_PAY";
    private static final String ORDER_STATUS_PAID = "PAID";
    private static final String ORDER_STATUS_REFUNDED = "REFUNDED";
    private static final String ORDER_STATUS_PARTIAL_REFUNDED = "PARTIAL_REFUNDED";
    private static final String PAYMENT_STATUS_SUCCESS = "SUCCESS";
    private static final String PAYMENT_STATUS_CANCELED = "CANCELED";

    /**
     * 1단계: 결제 준비 (주문 생성)
     */
    @Transactional
    public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request, Long userId) {
        if(request.getCourseIds().isEmpty()){
          throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }
        int size = request.getCourseIds().size();
      // 1. 가맹점 주문번호 생성 (ORD_날짜_UUID)
        String merchantUid = generateMerchantUid();

        boolean isCompleted = false;

        // 2. courseIds로 sessions 합계 금액 확인하기
        int cost = paymentDAO.getCostByCourseIds(request.getCourseIds(), userId);

        // 3-1. orderName 정하기
        TrainingCourse trainingCourse = courseDAO.getCourseById(request.getCourseIds().get(0));
        String lotOrderName = String.format("%s 외 %d 건", trainingCourse.getTitle(), size - 1);
        String notLotOrderName = trainingCourse.getTitle();
        String orderName = size > 1 ? lotOrderName : notLotOrderName;

        // 3-2. 주문 생성
        OrderMaster order = OrderMaster.builder()
                .userId(userId)
                .merchantUid(merchantUid)
                .orderStatus(ORDER_STATUS_READY_TO_PAY)
                .orderName(orderName)
                .totalAmount(cost)
                .paidAmount(0)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        orderDAO.insertOrder(order);

        // 4. 주문 item 생성
        if (order.getOrderId() == null) {
            throw new CustomException(ErrorCode.ORDER_CREATION_FAILED);
        }
        orderDAO.insertOrderItems(request.getCourseIds(),userId,order.getOrderId());

        if ( cost <= 0){
          isCompleted = true;
          PaymentApprovalRequest paymentApprovalRequest = PaymentApprovalRequest.builder()
                                                         .paymentKey(merchantUid)
                                                         .merchantUid(merchantUid)
                                                         .amount(cost)
                                                         .build();

          Map<String, Object> tossResponse = Map.of("method", "FREE",
                                                    "approvedAt", OffsetDateTime.now().toString(),
                                                    "orderName", orderName);
          savePayment(order, paymentApprovalRequest, tossResponse);
          updateOrderStatusToPaid(order,cost);
          updateApplicationStatus(order.getOrderId(), ORDER_STATUS_PAID);
        }

        return PaymentPrepareResponse.builder()
                .merchantUid(merchantUid)
                .amount(order.getTotalAmount())
                .isCompleted(isCompleted)
                .build();
    }

    @Transactional
    public PaymentApprovalResponse approvePayment(PaymentApprovalRequest request) {
        try {
            OrderMaster order = verifyOrder(request.getMerchantUid(), request.getAmount());
            Map<String, Object> tossResponse = callTossPaymentApi(request);
            Payment payment = savePayment(order, request, tossResponse);
            updateOrderStatusToPaid(order, request.getAmount());
            updateApplicationStatus(order.getOrderId(), ORDER_STATUS_PAID);

            return buildApprovalResponse(request, tossResponse);
        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            throw new CustomException(ErrorCode.PAYMENT_APPROVAL_FAILED);
        }
    }

    @Transactional
    public PaymentCancelResponse cancelPayment(PaymentCancelRequest request, Long userId) {
      Long owner = paymentDAO.findByPaymentKey(request.getPaymentKey())
                             .orElseThrow(()->new CustomException(ErrorCode.PAYMENT_NOT_FOUND)).getCreatedBy();
      boolean isTrainer = userDAO.isConnectedToTrainer(owner,userId);
      if(!Objects.equals(owner, userId) && !isTrainer){
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
      }

      try {
            Payment payment = findPayment(request.getPaymentKey());
            callTossCancelApi(request);
            updatePaymentStatusToCanceled(payment);
            updateOrderStatusAfterCancel(payment, request.getCancelAmount());
            updateApplicationStatus(payment.getOrderId(), "ACCEPT");


            return buildCancelResponse(request, payment);
        } catch (Exception e) {
            log.error("결제 취소 실패", e);
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    public List<PaymentLogResponse> getPaymentLogs(Long userId){
      return paymentDAO.findLogsByUserId(userId);
    }

    private OrderMaster verifyOrder(String merchantUid, Integer amount) {
        OrderMaster order = orderDAO.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getTotalAmount().equals(amount)) {
            log.error("금액 불일치 - DB: {}, 요청: {}", order.getTotalAmount(), amount);
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        return order;
    }

    private Map<String, Object> callTossPaymentApi(PaymentApprovalRequest request) throws Exception {
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getMerchantUid(),
                "amount", request.getAmount()
        ));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TOSS_API_URL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() != 200) {
            log.error("토스 API 호출 실패 - status: {}, body: {}", httpResponse.statusCode(), httpResponse.body());
            throw new RuntimeException("토스 결제 승인 실패");
        }

        return objectMapper.readValue(httpResponse.body(), Map.class);
    }

    private Payment savePayment(OrderMaster order, PaymentApprovalRequest request, Map<String, Object> tossResponse) {
        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .paymentKey(request.getPaymentKey())
                .method((String) tossResponse.get("method"))
                .amount(request.getAmount())
                .paymentStatus(PAYMENT_STATUS_SUCCESS)
                .merchantUid(request.getMerchantUid())
                .paidAt(parseDateTime((String) tossResponse.get("approvedAt")))
                .createdBy(order.getUserId())
                .updatedBy(order.getUserId())
                .build();
        paymentDAO.insertPayment(payment);

        if(payment.getPaymentId() != null) {
        PaymentLog paymentLog = PaymentLog.builder()
                                          .paymentId(payment.getPaymentId())
                                          .status(payment.getPaymentStatus())
                                          .amount(payment.getAmount())
                                          .pgTid(null)
                                          .failureReason(null)
                                          .merchantUid(payment.getMerchantUid())
                                          .createdBy(payment.getCreatedBy())
                                          .updatedBy(payment.getCreatedBy())
                                          .build();

        paymentDAO.insertPaymentLog(paymentLog);
        }
        return payment;
    }

    private void updateOrderStatusToPaid(OrderMaster order, Integer paidAmount) {
        order.updatePaymentStatus(ORDER_STATUS_PAID, paidAmount, LocalDateTime.now());
        orderDAO.updateOrderMaster(order);
    }

    private void updateApplicationStatus(Long orderId, String status){
      List<Long> applicationIds = paymentDAO.findOrderItemsByOrderId(orderId)
                                            .stream()
                                            .map(OrderItem::getApplicationId)
                                            .toList();
      trainingCourseApplicationDAO.updateStatuses(applicationIds, status);
    }

    private PaymentApprovalResponse buildApprovalResponse(PaymentApprovalRequest request, Map<String, Object> tossResponse) {
        return PaymentApprovalResponse.builder()
                .paymentKey(request.getPaymentKey())
                .orderId(request.getMerchantUid())
                .orderName((String) tossResponse.get("orderName"))
                .method((String) tossResponse.get("method"))
                .totalAmount(request.getAmount())
                .status(PAYMENT_STATUS_SUCCESS)
                .approvedAt(parseDateTime((String) tossResponse.get("approvedAt")))
                .build();
    }

    private Payment findPayment(String paymentKey) {
        return paymentDAO.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private Map<String, Object> callTossCancelApi(PaymentCancelRequest request) throws Exception {
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String requestBody = request.getCancelAmount() != null
                ? objectMapper.writeValueAsString(Map.of(
                "cancelReason", request.getCancelReason(),
                "cancelAmount", request.getCancelAmount()
        ))
                : objectMapper.writeValueAsString(Map.of(
                "cancelReason", request.getCancelReason()
        ));

        String url = String.format(TOSS_CANCEL_URL, request.getPaymentKey());
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() != 200) {
            log.error("토스 취소 API 호출 실패 - status: {}, body: {}", httpResponse.statusCode(), httpResponse.body());
            throw new RuntimeException("토스 결제 취소 실패");
        }

        return objectMapper.readValue(httpResponse.body(), Map.class);
    }

    private void updatePaymentStatusToCanceled(Payment payment) {
        payment.updateStatus(PAYMENT_STATUS_CANCELED);
        paymentDAO.updatePayment(payment);

        if(payment.getPaymentId() != null) {
          PaymentLog paymentLog = PaymentLog.builder()
                                            .paymentId(payment.getPaymentId())
                                            .status(payment.getPaymentStatus())
                                            .amount(payment.getAmount())
                                            .pgTid(null)
                                            .failureReason(null)
                                            .merchantUid(payment.getMerchantUid())
                                            .createdBy(payment.getCreatedBy())
                                            .updatedBy(payment.getCreatedBy())
                                            .build();

          paymentDAO.insertPaymentLog(paymentLog);
      }
    }

    private void updateOrderStatusAfterCancel(Payment payment, Integer cancelAmount) {
        OrderMaster order = orderDAO.findById(payment.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        String newStatus = cancelAmount != null ? ORDER_STATUS_PARTIAL_REFUNDED : ORDER_STATUS_REFUNDED;
        order.updatePaymentStatus(newStatus, order.getPaidAmount(), order.getPaidAt());
        orderDAO.updateOrderMaster(order);
    }

    private PaymentCancelResponse buildCancelResponse(PaymentCancelRequest request, Payment payment) {
        return PaymentCancelResponse.builder()
                .paymentKey(request.getPaymentKey())
                .orderId(payment.getMerchantUid())
                .status(PAYMENT_STATUS_CANCELED)
                .canceledAmount(request.getCancelAmount() != null ? request.getCancelAmount() : payment.getAmount())
                .canceledAt(LocalDateTime.now())
                .cancelReason(request.getCancelReason())
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateTimeStr);
            return LocalDateTime.now();
        }
    }

    /**
     * 가맹점 주문번호 생성
     * 형식: ORD_yyyyMMddHHmmss_UUID
     */
    private String generateMerchantUid() {
        String timestamp = LocalDateTime.now()
                .toString()
                .replaceAll("[-:T.]", "");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("ORD_%s_%s", timestamp, uuid);
    }
}
