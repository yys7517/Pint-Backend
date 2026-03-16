/**
 * File: null.java Path: com.example.pintbackend.service.S3Service
 * <p>
 * Outline: business Logic for Post
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.service;


import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.domain.post.exception.PostNotFoundException;
import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.LoginPagePostResponse;
import com.example.pintbackend.dto.XmpAnalysisResponse;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.GetAllPostResponse;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.dto.postDto.PostUserInfo;
import com.example.pintbackend.dto.postDto.UpdatePostRequest;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.global.exception.ForbiddenException;
import com.example.pintbackend.repository.PostLikeRepository;
import com.example.pintbackend.repository.PostRepository;
import com.example.pintbackend.repository.UserRepository;
import com.example.pintbackend.service.imageservice.ImageCompressionService;
import com.example.pintbackend.service.imageservice.ImageMetadata;
import com.example.pintbackend.service.imageservice.ImageMetadataService;
import com.example.pintbackend.service.s3service.S3Service;
import com.example.pintbackend.service.s3service.XmpAnalysisService;
import org.springframework.util.StringUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final XmpAnalysisService xmpAnalysisService;
    private final ImageMetadataService imageMetadataService;
    private final ImageCompressionService imageCompressionService;

    /**
     * create post 게시글 만들때는 presigned URL 반환해주기
     */
    @Transactional
    public void createPost(CreatePostRequest request, CustomUserDetails userDetails)
            throws IOException {
        String userEmail = userDetails.getEmail();

        // Email로 유저 가져오기
        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException(userEmail)
        );

        MultipartFile image = request.getImage();
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일은 필수입니다.");
        }

        // camera info
        ImageMetadata meta = imageMetadataService.extract(image);

        // upload original
        String imageKey = s3Service.uploadFile(image);
        
        // 압축, 그리고 s3버킷에 올리기
        byte[] compressedBytes = imageCompressionService.compress(image);
        log.info("[ImageCompression] original bytes: {}", image.getSize());
        log.info("[ImageCompression] original: {} KB, compressed: {} KB", image.getSize() / 1024, compressedBytes.length / 1024);
        String compressedImageKey = s3Service.uploadCompressedImage(compressedBytes);

        String filterKey = null;

        log.info("filterKey : {}", filterKey);

        if (request.getFilter() != null && !request.getFilter().isEmpty()) {
            filterKey = s3Service.uploadFile(request.getFilter());
        }

        // DTO->Create
        Post post = Post.builder()
                .description(request.getDescription())
                .location(request.getLocation())
                .imageFileS3Key(imageKey)
                .compressedImageFileS3Key(compressedImageKey)
                .filterFileS3Key(filterKey)
                .width(meta.width())
                .height(meta.height())
                .camera(meta.camera())
                .build();

        user.addPost(post);

        // DB에 저장하기
        postRepository.save(post);
    }

    /**
     * getAllPost Response: presignedUrl, and postId
     */
    public GetAllPostResponse getAllPost(CustomUserDetails userDetails, Pageable pageable) {

        Page<Post> posts = postRepository.findAll(pageable);

        List<PostImageResponse> content = posts.getContent().stream()
                .map(post ->
                        PostImageResponse.from(
                                post,
                                postLikeRepository.existsByPostIdAndUserId(post.getId(), userDetails.getUserId()),  // 게시글 좋아요 여부
                                resolvePresignedUrl(post.getCompressedImageFileS3Key() != null ? post.getCompressedImageFileS3Key() : post.getImageFileS3Key()),
                                new PostUserInfo(   // 게시글 작성자 정보
                                        post.getUser().getId(),
                                        post.getUser().getUsername(),
                                        resolvePresignedUrl(post.getUser().getProfileImageS3Key()),
                                        post.getUser().getId().equals(userDetails.getUserId())
                                )
                        ))
                .toList();

        return new GetAllPostResponse(
                content,
                posts.getNumber(),
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.hasNext(),
                posts.hasPrevious(),
                posts.isFirst(),
                posts.isLast()
        );
    }

    public List<LoginPagePostResponse> getLoginPagePosts() {
        List<Post> posts = postRepository.findTop10ByOrderByCreatedAtDesc();

        return posts.stream()
                .map(post -> {
                    String previewImageKey = StringUtils.hasText(post.getCompressedImageFileS3Key())
                            ? post.getCompressedImageFileS3Key()
                            : post.getImageFileS3Key();

                    return LoginPagePostResponse.from(
                            post,
                            resolvePresignedUrl(previewImageKey)
                    );
                })
                .toList();
    }

    public PostResponse getPostById(Long postId, CustomUserDetails userDetails) throws IOException {
        log.info("게시글를 id 로 불러오는중 {}", postId);

        Post post = postRepository.findById(postId).orElseThrow(() -> {
            log.warn("불러오는 페이지가 없습니다 {}", postId);
            return new PostNotFoundException(postId);
        });

        log.info("게시글를 성공적으로 불러왔습니다 {}", postId);

        String imageUrl = resolvePresignedUrl(post.getImageFileS3Key());

        String userProfileImageUrl = null;
        if (post.getUser().getProfileImageS3Key() != null && !post.getUser().getProfileImageS3Key()
                .isEmpty()) {
            userProfileImageUrl = resolvePresignedUrl(post.getUser().getProfileImageS3Key());
        }

        XmpAnalysisResponse xmpToJson = null;
        if (post.getFilterFileS3Key() != null && !post.getFilterFileS3Key().isEmpty()) {
            xmpToJson = xmpAnalysisService.analyze(post.getFilterFileS3Key());
        }

        // 게시글 작성자 정보
        PostUserInfo userInfo = new PostUserInfo(
                post.getUser().getId(),
                post.getUser().getUsername(),
                userProfileImageUrl,
                userDetails.getUserId().equals(post.getUser().getId())
        );

        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId,
                userDetails.getUserId());

        // 게시글 좋아요 개수
        int likeCount = postLikeRepository.countByPostId(postId);

        return PostResponse.from(post, userInfo, imageUrl, isLiked, likeCount, xmpToJson);
    }

    private String resolvePresignedUrl(String s3Key) {
        if (!StringUtils.hasText(s3Key)) {
            return null;
        }
        return s3Service.getPresignedUrlToRead(s3Key);
    }

    /**
     * editPostById
     * TODO: create editPostById function with description, location, filter(JSON), and camera request
     * TODO: repsonse -> description, location, filter(JSON), camera.
     */

    @Transactional
    public void updatePost(Long postId, CustomUserDetails userDetails, UpdatePostRequest request)
            throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 게시글 작성자와 현재 유저가 같나?
        if (!Objects.equals(post.getUser().getId(), userDetails.getUserId())) {
            throw new ForbiddenException("게시글를 수정할 권한이 없습니다.");
        }

        String filterKey = post.getFilterFileS3Key();
        boolean hasExistingFilter = filterKey != null && !filterKey.isEmpty();

        if (request.getFilter() != null && !request.getFilter().isEmpty()) {
            // Request Filter가 있고, 기존 필터가 있으면 삭제 후, 새 파일 업로드
            if (hasExistingFilter) {
                s3Service.deletePost(filterKey);
            }
            filterKey = s3Service.uploadFile(request.getFilter());
        } else {
            // Request Filter가 없을때
            if (hasExistingFilter) {  // 기존 필터가 있으면 제거
                s3Service.deletePost(filterKey);
            }
            filterKey = null;
        }

        post.update(
                request.getDescription(),
                request.getLocation(),
                request.getCamera(),
                filterKey
        );
    }


    /**
     * deletePost
     */
    @Transactional
    public void deletePost(Long postId, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 게시글 작성자와 현재 유저가 같나?
        if (!Objects.equals(post.getUser().getId(), userDetails.getUserId())) {
            throw new ForbiddenException("게시글를 삭제할 권한이 없습니다.");
        }

        // S3 에서 지우기
        if (post.getImageFileS3Key() != null && !post.getImageFileS3Key().isEmpty()) {
            s3Service.deletePost(post.getImageFileS3Key());
        }
        if (post.getCompressedImageFileS3Key() != null && !post.getCompressedImageFileS3Key().isEmpty()) {
            s3Service.deletePost(post.getCompressedImageFileS3Key());
        }
        if (post.getFilterFileS3Key() != null && !post.getFilterFileS3Key().isEmpty()) {
            s3Service.deletePost(post.getFilterFileS3Key());
        }

        // delete in db
        postRepository.delete(post);
    }
}
