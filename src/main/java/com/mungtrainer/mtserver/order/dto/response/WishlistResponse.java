package com.mungtrainer.mtserver.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long userId;
    private Long wishlistItemId;
    private Long wishlistId;
    private Long courseId;
    private Integer price;
    private String status;
    private Long dogId;
    private String dogName;
    private String title;
    private String type;
    private String schedule;
    private String location;
}
