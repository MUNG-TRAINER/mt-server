package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.entity.OrderItem;
import com.mungtrainer.mtserver.order.entity.OrderMaster;
import org.apache.ibatis.annotations.Mapper;

/**
 * 주문 Mapper
 */
@Mapper
public interface OrderDAO {

    /**
     * 주문 생성
     */
    void insertOrder(OrderMaster order);

    /**
     * 주문 항목 생성
     */
    void insertOrderItem(OrderItem orderItem);

    /**
     * 주문 상태 업데이트
     */
    void updateOrderStatus(OrderMaster order);
}