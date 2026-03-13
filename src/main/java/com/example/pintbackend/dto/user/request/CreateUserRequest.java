package com.example.pintbackend.dto.user.request;

import com.example.pintbackend.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;


public record CreateUserRequest (
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @NotBlank(message = "닉네임은 필수입니다.")
    String username
) {
  public User toEntity() {
    return User.builder()
        .email(email())
        .password(password())
        .username(username())
        .build();
  }
}
