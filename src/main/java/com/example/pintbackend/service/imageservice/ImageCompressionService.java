package com.example.pintbackend.service.imageservice;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ImageCompressionService {

    private static final int MAX_DIMENSION = 1080;
    private static final double JPEG_QUALITY = 0.80;

    /**
     * 이미지를 JPEG thumbnail 로 압축함, 최대 1080px on the longest side.
     * Thumbnailator 가 비율을 보존한다
     */
    public byte[] compress(MultipartFile file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(MAX_DIMENSION, MAX_DIMENSION)
                .outputFormat("jpg")
                .outputQuality(JPEG_QUALITY)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
