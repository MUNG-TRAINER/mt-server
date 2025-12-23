package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WishlistApplyRequest {
    private Long wishlistItemId;
    private Long dogId;
    private Long courseId;
}
