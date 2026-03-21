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
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.dto.postDto.UpdatePostRequest;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.PostService;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 작성",
            description = "이미지(필수)와 설명/위치/카메라/필터 정보를 받아 새 게시글을 생성합니다."
    )
    public ResponseEntity<BaseResponse<?>> createPost(
            @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {

        postService.createPost(request, userDetails);

        return ResponseEntity.ok(BaseResponse.success("게시글가 작성되였습니다!"));
    }

    @GetMapping
    @Operation(
            summary = "모든 게시글 조회",
            description = "페이지 기반으로 게시글 목록과 페이지네이션 정보를 조회합니다."
    )
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 크기(1~100 권장)", example = "10")
    })

    public ResponseEntity<BaseResponse<GetAllPostResponse>> getAllPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size는 1이상이여야 합니다.");
        }

        Pageable pageable = PageRequest.of(
                page,
                size
        );
        GetAllPostResponse response = postService.getAllPost(userDetails, pageable);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "게시글 상세 조회",
            description = "특정 게시글 1건과 좋아요 상태/개수, 메타데이터를 조회합니다."
    )
    public ResponseEntity<BaseResponse<PostResponse>> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        PostResponse response = postService.getPostById(postId, userDetails);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 수정",
            description = "특정 게시글의 설명/위치/카메라/필터 정보를 수정합니다."
    )

    public ResponseEntity<BaseResponse<?>> updatePost(
            @PathVariable Long postId,
            @ModelAttribute UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        postService.updatePost(postId, userDetails, request);

        return ResponseEntity.ok(BaseResponse.success("게시글가 수정되였습니다!"));
    }

    @DeleteMapping("/{postId}")
    @Operation(
            summary = "게시글 삭제",
            description = "특정 게시글과 연결된 이미지 자원을 삭제합니다(작성자만 가능)."
    )
    public ResponseEntity<BaseResponse<?>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.deletePost(postId, userDetails);

        return ResponseEntity.ok(BaseResponse.success("게시글가 정상적으로 지워졌습니다!"));
    }
}
