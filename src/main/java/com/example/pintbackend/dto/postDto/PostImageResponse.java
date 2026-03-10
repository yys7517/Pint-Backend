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
import com.example.pintbackend.service.s3service.S3Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PostImageResponse {

    private Long postId;
    private String imageUrl;

    public static PostImageResponse from(Post post, S3Service s3Service) {
        return PostImageResponse.builder()
                .postId(post.getId())
                .imageUrl(s3Service.getPresignedUrlToRead(post.getImageFileS3Key()))
                .build();
    }

    /**
     * 리스트 요청을 했을떄 포스트 아이디, presigned URL 반환하는 헬퍼 함수
     *
     * @param posts
     * @param s3Service
     * @return
     */
    public static List<PostImageResponse> fromList(List<Post> posts, S3Service s3Service) {
        return posts.stream()
                .map(post -> from(post, s3Service))
                .toList();
    }
}
