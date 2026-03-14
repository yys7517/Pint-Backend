package com.example.pintbackend.controller;

import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.postDto.profile.request.EditProfileRequest;
import com.example.pintbackend.dto.postDto.profile.response.EditProfileResponse;
import com.example.pintbackend.dto.postDto.profile.response.MyProfileResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PutMapping(value = "/{userId}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 수정")
    public BaseResponse<EditProfileResponse> editProfile(
            @PathVariable Long userId,
            @ModelAttribute EditProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        return BaseResponse.success(userService.editProfile(userId, userDetails, request));
    }
}
