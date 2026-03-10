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
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.repository.PostRepository;
import com.example.pintbackend.service.s3service.S3Service;
import com.example.pintbackend.service.s3service.XmpAnalysisService;
import lombok.RequiredArgsConstructor;
import org.hibernate.type.TrueFalseConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final XmpAnalysisService xmpAnalysisService;

    /**
     * create post
     * 포스트 만들때는 presigned URL 반환해주기
     *
     */
    public void createPost(CreatePostRequest request) throws IOException {

        // s3key -> actual image url
        try {
            String imageKey = s3Service.uploadFile(request.getImage());
            String fileKey = s3Service.uploadFile(request.getFilter());
            // DTO->Create
            Post post = Post.builder()
                    .description(request.getDescription())
                    .location(request.getLocation())
                    .imageFileS3Key(imageKey)
                    .filterFileS3Key(fileKey)
                    .createdAt(LocalDateTime.now())
                    .build();

            // DB에 저장하기
            Post saved = postRepository.save(post);

        } catch (IOException e) {
            throw new RuntimeException("ERROR: S3에 업로드를 실패했습니다");
        }

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

    public PostResponse getPostById(Long postId) throws IOException {

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("ERROR: 포스트가 없습니다"));

        // presigned Url
        String imageUrl = s3Service.getPresignedUrlToRead(post.getImageFileS3Key());

        XmpAnalysisResponse xmpToJson = xmpAnalysisService.analyze(post.getFilterFileS3Key());

        return PostResponse.from(post, imageUrl, xmpToJson);
    }

    /**
     * deletePost
     */
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("ERROR: 지울 포스트가 없습니다"));

        // S3 에서 지우기
        s3Service.deletePost(post.getImageFileS3Key());

        // delete in db
        postRepository.delete(post);
    }
}
