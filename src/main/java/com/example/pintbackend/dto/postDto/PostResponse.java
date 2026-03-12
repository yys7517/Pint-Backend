/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * post response
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;

import com.example.pintbackend.domain.Post;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {

    private Long id;
    private String description;
    private String location;
    private String imageUrl;     // presigned img URL

    private XmpAnalysisResponse filter;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post, String imageUrl, XmpAnalysisResponse xmlUrl) {
        return PostResponse.builder()
                .id(post.getId())
                .description(post.getDescription())
                .location(post.getLocation())
                .imageUrl(imageUrl)
                .filter(xmlUrl)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
