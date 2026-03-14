package com.example.pintbackend.controller;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.postDto.LikeResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final PostLikeService postLikeService;

    /**
     * 게시글 좋아요 toggle
     * @param postId
     * @param userDetails
     * @return
     */
    @PostMapping("{postId}")
    public ResponseEntity<BaseResponse<LikeResponse>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LikeResponse response = postLikeService.toggleLike(postId, userDetails);

        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
