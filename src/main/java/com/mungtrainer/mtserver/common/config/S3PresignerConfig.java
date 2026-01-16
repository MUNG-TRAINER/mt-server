package com.mungtrainer.mtserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {

  private final AwsS3Config awsS3Config;

  public S3PresignerConfig(AwsS3Config awsS3Config) {
    this.awsS3Config = awsS3Config;
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    awsS3Config.getAccessKey(),
                    awsS3Config.getSecretKey())))
        .region(Region.of(awsS3Config.getRegion()))
        .build();
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    awsS3Config.getAccessKey(),
                    awsS3Config.getSecretKey())))
        .region(Region.of(awsS3Config.getRegion()))
        .build();
  }
}