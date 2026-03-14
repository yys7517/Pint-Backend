/**
 * File: null.java
 * Path: com.example.pintbackend.dto.postDto.profile
 * <p>
 * Outline:
 * response
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.dto.postDto.profile.response;

import com.example.pintbackend.domain.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor

public class ProfileImageResponse {

    private Long postId;
    private String imageUrl;

    // mappping 로직을 DTO 에 두기 위해 from 을 쓴다
    public static ProfileImageResponse from(Post post, String imageUrl) {
        return ProfileImageResponse.builder()
                .postId(post.getId())
                .imageUrl(imageUrl)
                .build();
    }
}
