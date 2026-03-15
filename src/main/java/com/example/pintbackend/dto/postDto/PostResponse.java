package com.example.pintbackend.dto.postDto;

import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        PostUserInfo userInfo,
        String description,
        String location,
        String postImgUrl,
        String camera,
        @JsonProperty("isLiked") boolean isLiked,
        int likeCount,
        XmpAnalysisResponse filter,
        LocalDateTime createdAt
) {

    public static PostResponse from(
            Post post,
            PostUserInfo postUserInfo,
            String imageUrl,
            boolean isLiked,
            int likeCount,
            XmpAnalysisResponse filterInfo
    ) {
        return new PostResponse(
            post.getId(),
            postUserInfo,
            post.getDescription(),
            post.getLocation(),
            imageUrl,
            post.getCamera(),
            isLiked,
            likeCount,
            filterInfo,
            post.getCreatedAt()
        );
    }
}
