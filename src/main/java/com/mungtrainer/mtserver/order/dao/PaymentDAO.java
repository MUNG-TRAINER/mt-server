package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.entity.Payment;
import com.mungtrainer.mtserver.order.entity.PaymentLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 결제 Mapper
 */
@Mapper
public interface PaymentDAO {

    /**
     * 결제 생성
     */
    void insertPayment(Payment payment);

    /**
     * 결제 로그 생성
     */
    void insertPaymentLog(PaymentLog paymentLog);
}