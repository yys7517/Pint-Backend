/**
 * File: null.java
 * Path: com.example.pintbackend.service
 * <p>
 * Outline:
 * s3testservice
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service.s3service;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * MultipartFile 이미지 or XMP 파일을 S3에 업로드하고, s3Key 값을 반환한다.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // S3 객체 키(경로 + 파일명)를 생성한다.
        String s3Key = buildObjectKey(file);

        // S3에 저장할 메타데이터를 파일 기준으로 맞춘다.
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // MultipartFile의 실제 바이트를 S3에 업로드한다.
        // amazonS3.putObject가 실패한다면?
        try {
            amazonS3.putObject(bucket, s3Key, file.getInputStream(), metadata);  // 버킷 서버에 업로드
        } catch (IOException e) {
            throw new RuntimeException("ERROR: S3에 업로드를 실패했습니다", e);
        }

        return s3Key;  // DB에 저장을 위해 s3 key값 반환
    }

    /**
     * 업로드된 이미지를 로드하기(GET) 위한 Pre-signed URL을 생성한다.
     * 이 URL은 expiration 시점까지만 유효
     */
    public String getPresignedUrlToRead(String path) {
        // URL 만료 시간을 "현재 시각 + 5분"으로 설정한다.
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

        // 조회 목적 URL이므로 반드시 GET 메서드로 서명해야 한다.
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, path)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        // AWS 서명 정보가 포함된 임시 접근 URL 생성
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return url.toString();
    }

    /**
     * S3 객체 키를 생성한다. (FilePath)
     * - 파일명 충돌 방지: UUID 사용
     * - 원본 확장자 유지(.png, .jpg 등)
     */
    private String buildObjectKey(MultipartFile file) {
        String extension = resolveExtension(file);
        if (".xmp".equals(extension)) {
            return "xmp/" + UUID.randomUUID() + extension;
        }

        return "images/" + UUID.randomUUID() + extension;
    }

    /**
     * 확장자 우선순위:
     * 1) 원본 파일명 확장자
     * 2) Content-Type 기반 추론
     * 3) 없으면 .bin
     */

    private String resolveExtension(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0) {
                return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
            }
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return ".bin";
        }

        // TODO: 더 간격하게 하
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpeg";
            case "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "application/rdf+xml", "application/xml", "text/xml", "text/plain" -> ".xmp";
            default -> ".bin";
        };
    }

    /**
     * 게시글 지우기
     */

    public void deletePost(String key) {
        amazonS3.deleteObject(bucket, key);
    }
}
