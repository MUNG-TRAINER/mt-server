package com.mungtrainer.mtserver.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class WishlistApplyRequest {
    @NotNull(message = "wishlistItemId는 필수입니다.")
    private Long wishlistItemId;
    @NotNull(message = "dogId는 필수입니다.")
    private Long dogId;
    @NotNull(message = "courseId는 필수입니다.")
    private Long courseId;
}
