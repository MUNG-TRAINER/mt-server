package com.mungtrainer.mtserver.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class AwsS3TestConfig {

  @Bean
  public S3Client s3Client() {
    // 실제 AWS 호출 없이 Mock 객체 사용
    return mock(S3Client.class);
  }
}