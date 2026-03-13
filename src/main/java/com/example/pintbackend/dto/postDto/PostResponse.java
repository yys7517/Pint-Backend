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

import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PostResponse {

    private Long id;

    // User Info
    private PostUserInfo userInfo;

    private String description;
    private String location;
    private String postImgUrl;     // presigned img URL

    private XmpAnalysisResponse filter;
    private LocalDateTime createdAt;

    public static PostResponse from(
        Post post,
        PostUserInfo postUserInfo,
        String imageUrl,
        XmpAnalysisResponse filterInfo
    ) {
        return PostResponse.builder()
                .id(post.getId())
                .userInfo(postUserInfo)
                .description(post.getDescription())
                .location(post.getLocation())
                .postImgUrl(imageUrl)
                .filter(filterInfo)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
