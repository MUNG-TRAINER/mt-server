package com.mungtrainer.mtserver.common.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadUrlResponse {
  private String uploadUrl;
  private String fileKey;
}
