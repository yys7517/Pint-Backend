/**
 * File: null.java
 * Path: com.example.pintbackend.config
 * <p>
 * Outline:
 * s3 config
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    // S3Client 가 업로드랑 Delete operations 핸들함
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();       // 자동으로 credentials 이 IAM 유저로부터 로딩이 된다.
    }

    // SDK v2 의 다른 클라이언트, presigned URL 생성하느데만 쓰인다.
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}