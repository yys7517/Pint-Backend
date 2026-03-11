/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * only to return post id and image url
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;

import com.example.pintbackend.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostImageResponse {

    private Long id;
    private String imageUrl;

    public static PostImageResponse from(Post post, String imageUrl) {
        return PostImageResponse.builder()
                .id(post.getId())
                .imageUrl(imageUrl)
                .build();
    }
}
