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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
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
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cache.image-presign.s3-key-prefix:s3:presigned-url}")
    private String presignRedisKeyPrefix;

    @Value("${spring.cache.image-presign.expiration-minutes:60}")
    private long presignExpirationMinutes;

    @Value("${spring.cache.image-presign.redis-ttl-minutes:55}")
    private long presignRedisTtlMinutes;

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
        String normalizedPath = normalizeImageKey(path);
        if (!StringUtils.hasText(normalizedPath)) {
            return null;
        }

        String redisKey = buildPresignedUrlCacheKey(normalizedPath);

        // 1) Redis 캐시 조회: 값이 있으면 S3 presign를 다시 생성하지 않고 즉시 반환
        try {
            String cachedUrl = stringRedisTemplate.opsForValue().get(redisKey);
            if (cachedUrl != null) {
                return cachedUrl;
            }
        } catch (Exception e) {
            // Redis 장애가 발생해도 presigned URL 생성은 계속 동작해야 하므로 fallback 처리
            log.warn("Redis 캐시 조회 실패. Redis 키: {}, 예외: {}", redisKey, e.getMessage());
        }

        // 2) 캐싱된 URL이 없을 때, 새로 요청.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(normalizedPath)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        // 3) 캐시 저장: URL 만료 직전에 Redis가 먼저 만료되도록 보수적으로 TTL을 짧게 둠
        try {
            stringRedisTemplate.opsForValue().set(redisKey, presignedUrl, presignRedisTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패. Redis 키: {}, 예외: {}", redisKey, e.getMessage());
        }

        return presignedUrl;
    }

    /**
     * Redis에 사용할 presigned URL 캐시 key를 생성한다.
     * imageKey 전체를 key로 직접 쓰지 않고 prefix를 붙여 충돌 위험을 낮춘다.
     */
    private String buildPresignedUrlCacheKey(String path) {
        return presignRedisKeyPrefix + ":" + path;
    }

    /**
     * ImageKey를 캐시 key로 사용하기 전에 방어적으로 정규화한다.
     * - null/blank는 null 반환
     * - 양끝 공백만 제거
     */
    private String normalizeImageKey(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        return path.trim();
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
