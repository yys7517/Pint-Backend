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

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;


@Service
public class S3TestService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3TestService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String testUpload(MultipartFile file) throws IOException {
        String path = buildObjectKey(file);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        amazonS3.putObject(bucket, path, file.getInputStream(), metadata);

        // Private 버킷에서도 조회 가능한 URL (유효기간 있음)
        return getPresignedUrlToRead(path);
    }

    public String getPresignedUrlToRead(String path) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, path)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }

    private String buildObjectKey(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }
        return "images/profileImage-" + UUID.randomUUID() + extension;
    }
}
