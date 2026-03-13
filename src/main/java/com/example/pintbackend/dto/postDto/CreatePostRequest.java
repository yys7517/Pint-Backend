/**
 * File: null.java
 * Path: com.example.pintbackend.dto
 * <p>
 * Outline:
 * post request dto
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    private String description;
    private String location;

    private MultipartFile image;
    @Nullable private MultipartFile filter;
}
