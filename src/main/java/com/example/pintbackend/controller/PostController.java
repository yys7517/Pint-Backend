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
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "포스트 작성")
    public ResponseEntity<BaseResponse<?>> createPost(
        @ModelAttribute CreatePostRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {

        postService.createPost(request, userDetails);

        return ResponseEntity.ok(BaseResponse.success("포스트 가 작성되였습니다!"));
    }

    @GetMapping
    @Operation(summary = "모든 포스트 조회")
    public ResponseEntity<BaseResponse<GetAllPostResponse>> getAllPost() {
        List<PostImageResponse> posts = postService.getAllPost();
        GetAllPostResponse response = new GetAllPostResponse(posts);

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "포스트 수정하기")
    public ResponseEntity<BaseResponse<?>> updatePost(
            @PathVariable Long id,
            @ModelAttribute UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        postService.updatePost(id, userDetails, request);

        return ResponseEntity.ok(BaseResponse.success("포스트가 수정되였습니다!"));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "아이디로 포스트 지우기")
    public ResponseEntity<BaseResponse<?>> deletePost(@PathVariable Long id) {

        postService.deletePost(id);

        return ResponseEntity.ok(BaseResponse.success("포스트가 정상적으로 지워졌습니다!"));
    }


}
