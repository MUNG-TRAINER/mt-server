package com.mungtrainer.mtserver.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;
    private final AwsS3Config awsS3Config; // Config 주입
    private final S3Client s3Client;  // S3Client 추가

    /**
     * 단일 파일 조회용 Presigned URL 발급
     */
    public String generateDownloadPresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return null;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsS3Config.getBucket()) // Config에서 가져오기
                .key(fileKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(awsS3Config.getPresignedUrlExpirationMinutes()))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * 여러 파일 조회용 Presigned URL 발급
     */
    public List<String> generateDownloadPresignedUrls(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) return List.of();
        return fileKeys.stream()
                .map(this::generateDownloadPresignedUrl)
                .collect(Collectors.toList());
    }

    /**
     * S3에서 파일 삭제
     * S3 삭제 실패 시 예외를 던져 트랜잭션 롤백을 유도
     * @param fileKey S3 파일 키
     * @throws RuntimeException S3 삭제 실패 시
     */
    public void deleteFile(String fileKey) {
      if (fileKey == null || fileKey.isBlank()) {
        log.warn("삭제할 파일 키가 비어있습니다");
        return;
      }

      try {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(awsS3Config.getBucket())
            .key(fileKey)
            .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("S3 파일 삭제 완료 - key: {}", fileKey);
      } catch (S3Exception e) {
        log.error("S3 파일 삭제 실패 - key: {}, error: {}", fileKey, e.getMessage());
        throw new RuntimeException("S3 파일 삭제에 실패했습니다.", e);
      }
    }

    /**
     * 파일 업로드용 Presigned URL 생성
     * @param fileKey S3에 저장될 파일 키 (예: "dog-profiles/user-123/dog-456.jpg")
     * @param contentType 파일의 MIME 타입 (예: "image/jpeg")
     * @return 업로드용 Presigned URL
     */
    public String generateUploadPresignedUrl(String fileKey, String contentType) {
        if (fileKey == null || fileKey.isBlank()) {
            throw new IllegalArgumentException("파일 키는 필수입니다");
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Config.getBucket())
                .key(fileKey)
                .contentType(contentType) // MIME 타입 지정
                .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(awsS3Config.getPresignedUrlExpirationMinutes()))
                .build();

            String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();
            log.info("업로드용 Presigned URL 생성 완료 - key: {}, url: {}", fileKey, presignedUrl);

            return presignedUrl;
        } catch (S3Exception e) {
            log.error("Presigned URL 생성 실패 - key: {}, error: {}", fileKey, e.getMessage());
          throw new RuntimeException("Presigned URL 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.", e);
        }
    }

}
