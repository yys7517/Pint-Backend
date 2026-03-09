/**
 * File: null.java
 * Path: com.example.pintbackend.controller
 * <p>
 * Outline:
 * controller for s3 test
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import com.example.pintbackend.domain.Image;
import com.example.pintbackend.service.S3TestService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/s3")
public class S3TestController {

    private final S3TestService s3TestService;

    public S3TestController(S3TestService s3TestService) {
        this.s3TestService = s3TestService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createPost(@RequestParam("image") MultipartFile file) throws IOException {
        return s3TestService.testUpload(file);
    }

//    @GetMapping("/presigned")
//    public String getPresignedUrl(@RequestParam("image") MultipartFile file) throws IOException {
//        return s3TestService.createPresignedUrl(file);
//    }

//    @GetMapping("/test")
//    public String test() {
//        return s3TestService.testUpload();
//    }
}
