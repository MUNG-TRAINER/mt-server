package com.mungtrainer.mtserver.order.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistDogListResponse {
    private Long dogId;
    private String name;
    private String breed;
    private boolean hasCounseling;
}
