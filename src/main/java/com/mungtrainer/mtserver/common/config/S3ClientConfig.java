package com.mungtrainer.mtserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3ClientConfig {

    private final AwsS3Config awsS3Config;
    private final AwsCredentialsProvider credentialsProvider;

    public S3ClientConfig(AwsS3Config awsS3Config, AwsCredentialsProvider credentialsProvider) {
        this.awsS3Config = awsS3Config;
        this.credentialsProvider = credentialsProvider;
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(awsS3Config.getRegion()))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(awsS3Config.getRegion()))
                .build();
    }
}
