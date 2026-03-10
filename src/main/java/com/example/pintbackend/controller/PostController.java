/**
 * File: null.java
 * Path: com.example.pintbackend.controller
 * <p>
 * Outline:
 * API Layer
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import com.example.pintbackend.domain.Image;
import com.example.pintbackend.service.S3Service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Image createPost(@RequestParam("image") MultipartFile file) throws IOException {
        return imageService.createImage(file);
    }


    @GetMapping("/{id}")
    public Image getImageById(@PathVariable Long id) {
        Image image = imageService.getImage(id);
        return imageService.getImage(id);
    }


}
