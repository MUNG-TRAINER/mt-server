package com.mungtrainer.mtserver.common.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * S3 이미지 업로드 url 발급 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadUrlRequest {

  @NotBlank(message = "category는 필수입니다.")
  @Pattern(
      regexp = "^[a-z0-9-]+$",
      message = "category는 소문자, 숫자, 하이픈(-)만 사용할 수 있습니다."
  )
  private String category; // 예: dog-profile

  @NotBlank(message = "fileName은 필수입니다.")
  @Size(max = 150, message = "파일 제목은 150자 이하이어야 합니다.")
  @Pattern(
      regexp = "^[A-Za-z0-9가-힣._-]+\\.(jpg|jpeg|png|gif|webp|svg)$",
      message = "jpg, jpeg, png, gif, webp, svg 확장자만 허용됩니다."
  )
  private String fileName; // 예: mybestphoto.jpg


  @NotBlank(message = "콘텐츠 타입은 필수입니다")
  @Pattern(regexp = "^image/(jpg|jpeg|png|gif|webp|svg)$",
           message = "지원하는 이미지 형식은 image/jpeg, image/jpg, image/png, image/gif, image/webp, image/svg 입니다")
  private String contentType; // "image/jpeg", "image/png" 등
}
