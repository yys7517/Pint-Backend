/**
 * File: null.java
 * Path: com.example.pintbackend.service
 * <p>
 * Outline: Business logic layer
 * TODO:
 *  - upload to S3 Logic
 *  - extract metadata
 *  - save metadata to Postgres
 *  - invalidate Redis Cache
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;

import com.example.pintbackend.domain.Image;
import com.example.pintbackend.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private ImageRepository imageRepository;

    public Image getImage(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new RuntimeException("에러: 이미지를 찾을수 없습니다!"));
    }
}
