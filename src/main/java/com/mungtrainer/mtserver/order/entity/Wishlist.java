package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 장바구니 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Wishlist extends BaseEntity {

    /**
     * 장바구니 ID (PK)
     */
    private Long wishlistId;

    /**
     * 사용자 ID (FK - user.user_id)
     */
    private Long userId;

    /**
     * 상태 (ACTIVE, ORDERED, ABANDONED)
     */
    private String status;
}

