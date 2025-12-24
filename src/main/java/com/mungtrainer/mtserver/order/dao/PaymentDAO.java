package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.entity.OrderItem;
import com.mungtrainer.mtserver.order.entity.Payment;
import com.mungtrainer.mtserver.order.entity.PaymentLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 결제 Mapper
 */
@Mapper
public interface PaymentDAO {

  /**
  *  여러 코스의 세션들의 합계 금액 조회
  */
  int getCostByCourseIds(@Param("courseIds") List<Long> courseIds, @Param("userId") Long userId );

  /**
   *  orderId로 orderItem 리스트 조회
   */
  List<OrderItem> findOrderItemsByOrderId(Long orderId);

  /**
   * 결제 생성
   */
  void insertPayment(Payment payment);

  /**
   * 결제 로그 생성
   */
  void insertPaymentLog(PaymentLog paymentLog);

  /**
   * 결제 조회 (by paymentKey)
   */
  Optional<Payment> findByPaymentKey(String paymentKey);

  /**
   * 결제 정보 업데이트
   */
  void updatePayment(Payment payment);
}
