package com.example.pintbackend.dto.postDto;

public record PostUserInfo(
    Long userId,
    String username,
    String profileImage,
    Boolean isWriter
) {

}
