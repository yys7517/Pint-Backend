/**
 * File: null.java
 * Path: com.example.pintbackend.service
 * <p>
 * Outline:
 * s3testservice
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;


@Service
public class S3TestService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3TestService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String testUpload(MultipartFile file) {

        String content = "S3 connection success!";
        String path = "images/profileImage.";

        path += file.getOriginalFilename().split("\\.")[1];

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length());

        amazonS3.putObject(
                bucket,
                path,
                new ByteArrayInputStream(content.getBytes()),
                metadata
        );

        String presignedURL = getPresignedUrlToUpload(path);

//        return amazonS3.getUrl(bucket, path).toString();
        return presignedURL;
    }

    public String getPresignedUrlToUpload(String path) {

        // 2. URL 유효 시간 설정 (예: 5분)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5; // 5분 (ms 단위)
        expiration.setTime(expTimeMillis);

        // 3. PUT 메서드로 Pre-signed URL 요청 객체 생성
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, path)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        // 4. URL 생성 및 반환
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }
}
