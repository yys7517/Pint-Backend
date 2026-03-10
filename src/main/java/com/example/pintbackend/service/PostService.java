/**
 * File: null.java
 * Path: com.example.pintbackend.service.S3Service
 * <p>
 * Outline:
 * business Logic for Post
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;

import com.example.pintbackend.domain.Post;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.repository.PostRepository;
import com.example.pintbackend.service.s3service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;

    /**
     * create post
     * 포스트 만들때는 presigned URL 반환해주기
     *
     */
    public PostResponse createPost(CreatePostRequest request) throws IOException {

        // s3key -> actual image url
        String imageKey = s3Service.uploadFile(request.getImage());

        // DTO->Create
        Post post = Post.builder()
                .description(request.getDescription())
                .location(request.getLocation())
                .imageFileS3Key(imageKey)
                .createdAt(LocalDateTime.now())
                .build();

        // DB에 저장하기
        Post saved = postRepository.save(post);

        // presigned url
        String imageUrl = s3Service.getPresignedUrlToRead(saved.getImageFileS3Key());

        return PostResponse.from(saved, imageUrl);
    }

    /**
     * getAllPost
     */

    public List<PostImageResponse> getAllPost() {

        List<Post> posts = postRepository.findAll();

        return PostImageResponse.fromList(posts, s3Service);
    }

    /**
     * getPostById
     */

}
