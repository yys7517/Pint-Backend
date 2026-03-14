package com.example.pintbackend.controller;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.postDto.profile.MyProfileResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "내 프로필 페이지 상세")
    public BaseResponse<MyProfileResponse> getMyProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return BaseResponse.success(userService.getProfile(userId, userDetails));
    }

}
