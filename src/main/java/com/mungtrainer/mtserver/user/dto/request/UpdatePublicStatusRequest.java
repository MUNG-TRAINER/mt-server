package com.mungtrainer.mtserver.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 반려견 프로필 공개 여부 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePublicStatusRequest {

    /**
     * 사용자 반려견 프로필 공개 여부 (true: 공개, false: 비공개)
     */
    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isPublic;
}

