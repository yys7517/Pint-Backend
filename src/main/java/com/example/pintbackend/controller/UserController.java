package com.example.pintbackend.controller;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.dto.user.response.CheckDuplicateEmailResponse;
import com.example.pintbackend.dto.user.response.LoginUserResponse;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.user.request.CreateUserRequest;
import com.example.pintbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<BaseResponse<?>> signupUser(
      @Valid @RequestBody CreateUserRequest request
  ) {
    User user = request.toEntity();

    userService.signupUser(user);

    return ResponseEntity.ok(BaseResponse.success(""));
  }

  @PostMapping("/unique")
  public ResponseEntity<BaseResponse<CheckDuplicateEmailResponse>> checkDuplicateEmail(
      @RequestParam ("email") String email
  ) {
    boolean isAvailable = userService.isAvailableEmail(email);
    CheckDuplicateEmailResponse response = new CheckDuplicateEmailResponse(isAvailable);

    return ResponseEntity.ok(BaseResponse.success(response));
  }
}
