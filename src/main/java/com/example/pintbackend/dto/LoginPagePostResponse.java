package com.example.pintbackend.dto;

import com.example.pintbackend.domain.post.Post;

public record LoginPagePostResponse(
        String height,
        String width,
        String imageUrl
) {
    public static LoginPagePostResponse from(Post post, String imageUrl) {
        return new LoginPagePostResponse(
                post.getHeight() == null ? null : String.valueOf(post.getHeight()),
                post.getWidth() == null ? null : String.valueOf(post.getWidth()),
                imageUrl
        );
    }
}
