/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto
 * <p>
 * Outline:
 * only to return post id and image url, and now height, width, and
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto;

import com.example.pintbackend.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PostImageResponse {

    private Long id;
    private Long height;
    private Long width;
    private String imageUrl;
    private String camera;
    private String location;

    public static PostImageResponse from(Post post, String imageUrl) {
        return PostImageResponse.builder()
                .id(post.getId())
                .height(post.getHeight())
                .width(post.getWidth())
                .location(post.getLocation())
                .camera(post.getCamera())
                .imageUrl(imageUrl)
                .build();
    }

}
