/**
 * File: null.java Path: com.example.pintbackend.controller
 * <p>
 * Outline: controller for s3 test
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.service.S3TestService;
import com.example.pintbackend.service.XmpAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3TestController {
  private final S3TestService s3TestService;
  private final XmpAnalysisService xmpAnalysisService;

  @PostMapping(value = "image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String createPost(@RequestParam("image") MultipartFile file) throws IOException {
    String s3Key = s3TestService.uploadFile(file);
    return s3TestService.getPresignedUrlToRead(s3Key);  // Image Load를 위한 PresignedURL
  }

  @PostMapping(value = "xmp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public XmpAnalysisResponse createXMP(@RequestParam("xmp") MultipartFile file) throws IOException {
    String s3Key = s3TestService.uploadFile(file);
    return xmpAnalysisService.analyze(s3Key);   //  XMP 파일을 analyze
  }
}
