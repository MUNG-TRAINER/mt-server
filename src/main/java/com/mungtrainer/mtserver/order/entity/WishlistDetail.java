package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import jakarta.validation.constraints.Pattern;
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
     * 위시리스트 항목 ID (PK)
     */
    private Long wishlistItemId;

    /**
     * 장바구니 ID (FK - wishlist.wishlist_id)
     */
    private Long wishlistId;

    /**
     * 코스 ID (FK - training_course.course_id)
     */
    private Long courseId;

    /**
     * 가격
     */
    private Integer price;

    /**
     * 상태 (ACTIVE, ORDERED, ABANDONED)
     */
    @Pattern(regexp = "ACTIVE|ORDERED|ABANDONED",
            message = "유효하지 않은 상태입니다.")
    private String status;
}

