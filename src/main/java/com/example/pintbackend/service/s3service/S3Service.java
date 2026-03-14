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


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

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

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            // .frominputstream wraps multipart bytess for sdk v2
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(),
                    file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("ERROR: S3 업로드를 실패했습니다", e);
        }

        return s3Key;  // DB에 저장을 위해 s3 key값 반환
    }

    /**
     * 업로드된 이미지를 로드하기(GET) 위한 Pre-signed URL을 생성한다.
     * 이 URL은 expiration 시점까지만 유효
     */
    public String getPresignedUrlToRead(String path) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * s3 버킷에 압춘된 이미지 JPEGG 로 저장,
     */
    public String uploadCompressedImage(byte[] data) {
        String s3Key = "images/compressed/" + UUID.randomUUID() + ".jpg";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType("image/jpeg")
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

        return s3Key;
    }

    public void deletePost(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
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


}
