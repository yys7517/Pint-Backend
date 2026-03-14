/**
 * File: null.java
 * Path: com.example.pintbackend.controller
 * <p>
 * Outline:
 * API Layer
 * <p>
 * Author: jskt
 */

package com.example.pintbackend.controller;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.GetAllPostResponse;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.dto.postDto.UpdatePostRequest;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.PostService;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    // @ModelAttribute doesnt work with swagger
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 작성")
    public ResponseEntity<BaseResponse<?>> createPost(
            @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {

        postService.createPost(request, userDetails);

        return ResponseEntity.ok(BaseResponse.success("게시글 가 작성되였습니다!"));
    }

    @GetMapping
    @Operation(summary = "모든 게시글 조회")
    public ResponseEntity<BaseResponse<GetAllPostResponse>> getAllPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Direction.DESC
            ) Pageable pageable
    ) {
        GetAllPostResponse response = postService.getAllPost(userDetails, pageable);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회")
    public ResponseEntity<BaseResponse<PostResponse>> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        PostResponse response = postService.getPostById(postId, userDetails);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * editing post
     */

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 수정하기")
    public ResponseEntity<BaseResponse<?>> updatePost(
            @PathVariable Long postId,
            @ModelAttribute UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        postService.updatePost(postId, userDetails, request);

        return ResponseEntity.ok(BaseResponse.success("게시글가 수정되였습니다!"));
    }


    @DeleteMapping("/{postId}")
    @Operation(summary = "아이디로 게시글 지우기")
    public ResponseEntity<BaseResponse<?>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.deletePost(postId, userDetails);

        return ResponseEntity.ok(BaseResponse.success("게시글가 정상적으로 지워졌습니다!"));
    }


}
