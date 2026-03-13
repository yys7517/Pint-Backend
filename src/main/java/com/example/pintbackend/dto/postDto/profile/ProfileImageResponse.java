/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto.profile
 * <p>
 * Outline:
 * response
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto.profile;

import com.example.pintbackend.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor

public class ProfileImageResponse {

    private Long id;
    private String imageUrl;

    public static ProfileImageResponse from(Post post, String imageUrl) {
        return ProfileImageResponse.builder()
                .id(post.getId())
                .imageUrl(imageUrl)
                .build();
    }
}
