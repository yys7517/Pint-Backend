package com.example.pintbackend.controller;

import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.service.S3Service.XmpAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/xmp")
@RequiredArgsConstructor
public class XmpController {
    private final XmpAnalysisService xmpAnalysisService;

    /**
     * 사용자가 업로드한 XMP 파일을 분석해 변경된 보정값만 반환한다.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            XmpAnalysisResponse result = xmpAnalysisService.analyze(file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}
