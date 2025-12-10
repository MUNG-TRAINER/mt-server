package com.mungtrainer.mtserver.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;
    private final AwsS3Config awsS3Config; // Config 주입

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
                .signatureDuration(Duration.ofMinutes(10))
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
}
