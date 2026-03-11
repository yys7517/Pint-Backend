package com.example.pintbackend.controller;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.dto.user.response.CheckDuplicateEmailResponse;
import com.example.pintbackend.dto.user.response.LoginUserResponse;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.user.request.CreateUserRequest;
import com.example.pintbackend.global.jwt.dto.JwtTokenInfo;
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

  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginUserResponse>> loginUser(
      @Valid @RequestBody LoginUserRequest request
  ) {
    String email = request.email();
    String password = request.password();

    log.info("request username = {}, password = {}", email, password);

    JwtTokenInfo tokenInfo = userService.signin(email, password);
    String accessToken = tokenInfo.getAccessToken();
    String refreshToken = tokenInfo.getRefreshToken();

    Long userId = userService.getUserId(email);

    log.info("jwtToken accessToken = {}, refreshToken = {}", accessToken, refreshToken);
    // 2. new 키워드를 사용하여 record 객체 생성
    LoginUserResponse response = new LoginUserResponse(
        userId,
        accessToken
    );

    // 3. 컨트롤러 반환값(return) 추가
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshToken) // 안전하게 refresh Token은 쿠키 저장소에 저장.
        .body(BaseResponse.success(response));
  }
}
