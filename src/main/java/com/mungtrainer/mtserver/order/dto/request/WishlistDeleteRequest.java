package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WishlistDeleteRequest {
    private List<Long> wishlistItemId;
}
