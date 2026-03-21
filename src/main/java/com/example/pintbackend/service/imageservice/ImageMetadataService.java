/**
 * File: null.java
 * Path: com.example.pintbackend.service
 * <p>
 * Outline:
 * extracts metadata of an image file by using a library
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service.imageservice;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageMetadataService {

    public ImageMetadata extract(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());

            // 카메라 정보 EXIF IFD0 통해 불러오기
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            String camera = null;
            if (ifd0 != null) {
                String make = ifd0.getString(ExifIFD0Directory.TAG_MAKE);
                String model = ifd0.getString(ExifIFD0Directory.TAG_MODEL);
                camera = buildCameraString(make, model);
            }

            // dimensions: try EXIF -> JPEG -> PNG
            Long width = null;
            Long height = null;

            ExifSubIFDDirectory exifSub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSub != null
                    && exifSub.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)
                    && exifSub.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)) {
                width = exifSub.getLong(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                height = exifSub.getLong(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            }

            // case: no info given
            if (width == null) {
                JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
                if (jpegDirectory != null) {
                    width = (long) jpegDirectory.getImageWidth();
                    height = (long) jpegDirectory.getImageHeight();
                }
            }

            if (width == null) {
                PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
                if (pngDirectory != null) {
                    width = (long) pngDirectory.getInt(PngDirectory.TAG_IMAGE_WIDTH);
                    height = (long) pngDirectory.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
                }
            }

            return new ImageMetadata(width, height, camera);

        } catch (Exception e) {
            e.printStackTrace();
            return new ImageMetadata(null, null, null);
        }
    }

    // 카메라 정보 예쁘게 불러오는 헬퍼 함수
    private String buildCameraString(String make, String model) {

        if (make == null && model == null) {
            return null;
        }
        if (make == null) {
            return model.trim();
        }
        if (model == null) {
            return make.trim();
        }
        if (model.toLowerCase().startsWith(make.toLowerCase().trim())) {
            return model.trim();
        }

        return (make + " " + model).trim();
    }
}
