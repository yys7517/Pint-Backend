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
import com.example.pintbackend.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @GetMapping("/{id}")
    public Image getImageById(@PathVariable Long id) {
        Image image = imageService.getImage(id);
        return imageService.getImage(id);
    }
}
