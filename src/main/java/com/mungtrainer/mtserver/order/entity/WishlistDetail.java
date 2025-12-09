package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 장바구니 상세 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WishlistDetail extends BaseEntity {

    /**
     * 장바구니 항목 ID (PK)
     */
    private Long cartItemId;

    /**
     * 장바구니 ID (FK - wishlist.wishlist_id)
     */
    private Long wishlistId;

    /**
     * 신청 ID (FK - training_course_application.application_id)
     */
    private Long applicationId;

    /**
     * 가격
     */
    private Integer price;
}

