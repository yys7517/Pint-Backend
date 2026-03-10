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

import com.example.pintbackend.domain.Post;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.postDto.CreatePostRequest;
import com.example.pintbackend.dto.postDto.PostImageResponse;
import com.example.pintbackend.dto.postDto.PostResponse;
import com.example.pintbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "포스트 작성")
    public ResponseEntity<BaseResponse<?>> createPost(@ModelAttribute CreatePostRequest request) throws IOException {

        postService.createPost(request);

        return ResponseEntity.ok(BaseResponse.success("포스트 가 작성되였습니다!"));
    }

    @GetMapping
    @Operation(summary = "모든 포스트 조회")
    public ResponseEntity<BaseResponse<List<PostImageResponse>>> getAllPost() {

        List<PostImageResponse> posts = postService.getAllPost();

        return ResponseEntity.ok(BaseResponse.success(posts));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "아이디로 포스트 조회")
    public ResponseEntity<BaseResponse<PostResponse>> getPostById(@PathVariable Long postId) throws IOException {

        PostResponse post = postService.getPostById(postId);

        return ResponseEntity.ok(BaseResponse.success(post));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "아이디로 포스트 지우기")
    public ResponseEntity<BaseResponse<?>> deletePost(@PathVariable Long id) {

        postService.deletePost(id);

        return ResponseEntity.ok(BaseResponse.success("포스트가 정상적으로 지워졌습니다!"));
    }




}
