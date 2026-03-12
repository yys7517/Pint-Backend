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
import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.dto.postDto.UpdatePostRequest;
import com.example.pintbackend.repository.PostRepository;
import com.example.pintbackend.repository.UserRepository;
import com.example.pintbackend.service.imageservice.ImageMetadata;
import com.example.pintbackend.service.imageservice.ImageMetadataService;
import com.example.pintbackend.service.s3service.S3Service;
import com.example.pintbackend.service.s3service.XmpAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final XmpAnalysisService xmpAnalysisService;
    private final ImageMetadataService imageMetadataService;

    /**
     * create post
     * 포스트 만들때는 presigned URL 반환해주기
     *
     */
    @Transactional
    public void createPost(CreatePostRequest request, Authentication authentication) throws IOException {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow(
            () -> new UserNotFoundException(userEmail)
        );

        // camera info
        ImageMetadata meta = imageMetadataService.extract(request.getImage());

        // key -> actual image url
        String imageKey = s3Service.uploadFile(request.getImage());
        String fileKey = s3Service.uploadFile(request.getFilter());

        // DTO->Create
        Post post = Post.builder()
                .description(request.getDescription())
                .location(request.getLocation())
                .imageFileS3Key(imageKey)
                .filterFileS3Key(fileKey)
                .width(meta.width())
                .height(meta.height())
                .camera(meta.camera())
                .build();

        user.addPost(post);

        // DB에 저장하기
        postRepository.save(post);
    }

    /**
     * getAllPost
     * Response: presignedUrl, and postId
     */
    public List<PostImageResponse> getAllPost() {

        List<Post> posts = postRepository.findAll();

        return posts.stream()
                .map(post -> PostImageResponse.from(
                        post,
                        s3Service.getPresignedUrlToRead(post.getImageFileS3Key())
                ))
                .toList();
    }

    /**
     * getPostById
     */
    public PostResponse getPostById(Long postId) throws IOException {

        log.info("포스트를 id 로 불러오는중 {}", postId);

        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("불러오는 페이지가 없습니다 {}", postId);
            return new RuntimeException("ERROR: 포스트가 없습니다");
        });

        log.info("포스트를 성공적으로 불러왔습니다 {}", postId);

        // 상세페이지에 추가로 반환할 정보들 (이미지, 필터 파일)
        String imageUrl = s3Service.getPresignedUrlToRead(post.getImageFileS3Key());
        XmpAnalysisResponse xmpToJson = xmpAnalysisService.analyze(post.getFilterFileS3Key());

        return PostResponse.from(post, imageUrl, xmpToJson);
    }

    /**
     * editPostById
     * TODO: create editPostById function with description, location, filter(JSON), and camera request
     * TODO: repsonse -> description, location, filter(JSON), camera.
     */

    @Transactional
    public void updatePost(Long postId, UpdatePostRequest request) throws IOException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("ERROR: 수정할 포스트가 없습니다"));


        String ImageKey = post.getImageFileS3Key();
        String FilterKey = post.getFilterFileS3Key();

        // 이미지 충돌나지 않도록 지우기.
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            s3Service.deletePost(post.getImageFileS3Key());
            ImageKey = s3Service.uploadFile(request.getImage());
        }

        if (request.getFilter() != null && !request.getFilter().isEmpty()) {
            s3Service.deletePost(post.getFilterFileS3Key());
            FilterKey = s3Service.uploadFile(request.getFilter());
        }

        post.update(request.getDescription(),
                request.getLocation(),
                ImageKey,
                FilterKey
        );
    }


    /**
     * deletePost
     */
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("ERROR: 지울 포스트가 없습니다"));

        // S3 에서 지우기
        s3Service.deletePost(post.getImageFileS3Key());
        s3Service.deletePost(post.getFilterFileS3Key());

        // delete in db
        postRepository.delete(post);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("ERROR: 로그인한 사용자만 포스트를 작성할 수 있습니다");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("ERROR: 로그인 사용자를 찾을 수 없습니다"));
    }
}
