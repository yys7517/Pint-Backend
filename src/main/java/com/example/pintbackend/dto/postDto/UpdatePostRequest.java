/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * dto for update post request
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    private String description;
    private String location;
    private String camera;

    @Schema(type = "string", format = "binary")
    private MultipartFile filter;
}
