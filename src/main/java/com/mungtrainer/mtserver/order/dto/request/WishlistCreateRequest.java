package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistCreateRequest {
    private Long courseId;
    private Long dogId;
}
