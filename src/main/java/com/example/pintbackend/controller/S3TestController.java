/**
 * File: null.java Path: com.example.pintbackend.controller
 * <p>
 * Outline: controller for s3 test
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.service.s3service.S3Service;
import com.example.pintbackend.service.s3service.XmpAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3TestController {
  private final S3Service s3Service;
  private final XmpAnalysisService xmpAnalysisService;

  @PostMapping(value = "image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String createPost(@RequestParam("image") MultipartFile file) throws IOException {
    String s3Key = s3Service.uploadFile(file);
    return s3Service.getPresignedUrlToRead(s3Key);  // Image Load를 위한 PresignedURL
  }

  @PostMapping(value = "xmp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public XmpAnalysisResponse createXMP(@RequestParam("xmp") MultipartFile file) throws IOException {
    String s3Key = s3Service.uploadFile(file);
    return xmpAnalysisService.analyze(s3Key);   //  XMP 파일을 analyze
  }
}
