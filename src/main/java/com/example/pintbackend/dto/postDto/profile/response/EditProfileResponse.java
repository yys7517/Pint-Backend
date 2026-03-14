package com.example.pintbackend.dto.postDto.profile.response;

import com.example.pintbackend.domain.user.entity.User;

public record EditProfileResponse(
        String username,
        String introduction,
        String city,
        String profileImage
) {
    // user.update() 후 불러오기 때 필드 정보는 이미 최신 유지
    public static EditProfileResponse from(User user, String profileImageUrl) {
        return new EditProfileResponse(
                user.getUsername(),
                user.getIntroduction(),
                user.getCity(),
                profileImageUrl
        );
    }
}
