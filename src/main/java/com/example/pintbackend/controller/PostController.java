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
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.service.s3service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> createPost(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("filter") MultipartFile filterFile,
            @RequestParam("location") String location,
            @RequestParam("description") String description) throws IOException {


        return ResponseEntity.ok(BaseResponse.success(""));
    }


}
