package com.mungtrainer.mtserver.common.s3;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class S3Controller {
  private final S3UploadService s3UploadService;

  /**
   * 이미지 업로드용 Presigned URL 발급 (신규 등록용)
   * @param request 파일 키 및 메타정보
   * @return 업로드 URL
   */
  @PostMapping("/presigned-url")
  public ResponseEntity<String> generateUploadUrl(
      @Valid @RequestBody UploadUrlRequest request,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    String responseUrl = s3UploadService.generateUploadPresignedUrl(request, customUserDetails.getUserId());
    return ResponseEntity.ok(responseUrl);
  }
}
