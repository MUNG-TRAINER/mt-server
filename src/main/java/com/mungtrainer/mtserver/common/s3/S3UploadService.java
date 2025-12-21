package com.mungtrainer.mtserver.common.s3;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class S3UploadService {
  private final S3Service s3Service;

  public UploadUrlResponse generateUploadPresignedUrl(UploadUrlRequest request, Long userId) {
    if (userId == null ||
        request.getContentType() == null ||
        request.getCategory() == null ||
        request.getFileName() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST_DATA);
    }
    String fileName = request.getFileName();
    int dotIndex = fileName.lastIndexOf('.');
    String name = fileName.substring(0, dotIndex);
    String extension = fileName.substring(dotIndex);
    long timestamp = System.currentTimeMillis();
    fileName = "%s-%d%s".formatted(name, timestamp, extension);

    String fileKey = "%s/%d/%s".formatted(request.getCategory(), userId, fileName);
    String url = s3Service.generateUploadPresignedUrl(fileKey, request.getContentType());

    return UploadUrlResponse.builder()
                            .uploadUrl(url)
                            .fileKey(fileKey)
                            .build();
  }

}
