package com.mungtrainer.mtserver.order.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WishlistDetailDog  extends BaseEntity {
    private Long wishlistItemId;
    private Long dogId;
}
