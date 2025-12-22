package com.mungtrainer.mtserver.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AwsS3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presigned-url.expiration-minutes:10}")
    private int presignedUrlExpirationMinutes;
}
