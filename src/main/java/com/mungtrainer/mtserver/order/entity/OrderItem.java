package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 주문 항목 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {

    /**
     * 주문 항목 ID (PK)
     */
    private Long orderItemId;

    /**
     * 주문 ID (FK - order_master.order_id)
     */
    private Long orderId;

    /**
     * 신청 ID (FK - training_course_application.application_id)
     */
    private Long applicationId;

    /**
     * 가격
     */
    private Integer price;
}

