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

package com.example.pintbackend.service.S3Service;

import com.example.pintbackend.domain.Image;
import com.example.pintbackend.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {


    private final ImageRepository imageRepository;
    private final String uploadDir = "upload/";

    public List<Image> getAllimage() {
        return imageRepository.findAll();
    }

    public Image getImage(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new RuntimeException("에러: 이미지를 찾을수 없습니다!"));
    }

    public Image createImage(MultipartFile image) throws IOException {

        // 1. 업로드 장소 없으면 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 2. 파일 저장
        String imagePath = null;

        if (image != null && !image.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File dest = new File(dir, fileName);
            System.out.println("uploadDir = " + dir.getAbsolutePath());
            System.out.println("dest = " + dest.getAbsolutePath());

            Files.copy(image.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            imagePath = "/upload/" + fileName;
        }

        Image img = new Image();
        img.setImgUrl(imagePath);

        return imageRepository.save(img);
    }
}
