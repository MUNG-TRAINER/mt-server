package com.mungtrainer.mtserver.dog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 반려견 프로필 이미지 업로드 URL 발급 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DogImageUploadRequest {

    @NotBlank(message = "파일 키는 필수입니다")
    @Size(max = 500, message = "파일 키는 500자 이내로 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9/_.-]+$",
             message = "파일 키는 영문, 숫자, /, _, ., - 만 사용 가능합니다")
    private String fileKey; // 프론트가 결정한 S3 키 (예: "dog-profiles/user-2/temp-1704067200000.jpg")

    @NotBlank(message = "콘텐츠 타입은 필수입니다")
    @Pattern(regexp = "^image/(jpeg|png|gif|webp)$",
             message = "지원하는 이미지 형식은 image/jpeg, image/png, image/gif, image/webp입니다")
    private String contentType; // "image/jpeg", "image/png" 등
}