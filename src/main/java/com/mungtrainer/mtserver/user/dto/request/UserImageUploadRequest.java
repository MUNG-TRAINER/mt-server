package com.mungtrainer.mtserver.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserImageUploadRequest {

    @NotBlank(message = "파일 키는 필수입니다")
    private String fileKey;

    @NotBlank(message = "Content-Type은 필수입니다")
    @Pattern(regexp = "^image/(jpeg|png|gif|webp)$",
             message = "지원하지 않는 이미지 형식입니다")
    private String contentType;
}