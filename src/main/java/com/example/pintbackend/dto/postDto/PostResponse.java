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
import com.example.pintbackend.service.s3service.S3Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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

//    public static List<PostResponse> fromList(List<Post> posts, S3Service s3Service) {
//        return posts.stream()
//                .map(post -> PostResponse.from(
//                        post,
//                        s3Service.getPresignedUrlToRead(post.getImageFileS3Key()),
//                        post.getFilterFileS3Key() != null
//                                ? s3Service.getPresignedUrlToRead(post.getFilterFileS3Key())
//                                : null
//                ))
//                .toList();
//    }
}
