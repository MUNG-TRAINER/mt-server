package com.mungtrainer.mtserver.order.dao;

import com.mungtrainer.mtserver.order.entity.OrderItem;
import com.mungtrainer.mtserver.order.entity.OrderMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

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
     * 주문 item 생성
     */
    void insertOrderItems(@Param("applicationIds") List<Long> applicationIds, Long userId, Long orderId);

  /**
     * 주문 항목 생성
     */
    void insertOrderItem(OrderItem orderItem);

    /**
     * 주문 상태 업데이트
     */
    void updateOrderStatus(OrderMaster order);

    /**
     * 주문 조회 (by merchantUid)
     */
    Optional<OrderMaster> findByMerchantUid(String merchantUid);

    /**
     * 주문 마스터 정보 업데이트
     */
    void updateOrderMaster(OrderMaster order);

    /**
     * 주문 조회 (by orderId)
     */
    Optional<OrderMaster> findById(Long orderId);
}
