package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {
    @NotNull(message = "dogId는 필수입니다.")
    private Long dogId;

    // 장바구니에서 넘어온 경우만 값이 있음, 선택적
    private Long wishlistItemId;
}
