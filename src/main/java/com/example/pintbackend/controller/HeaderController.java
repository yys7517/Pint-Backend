package com.example.pintbackend.controller;

import com.example.pintbackend.dto.header.response.HeaderProfileImgResponse;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/header")
@RequiredArgsConstructor
@Tag(name = "Header", description = "헤더 영역 API")
public class HeaderController {

  private final UserService userService;

  @GetMapping("/profile")
  @Operation(
      summary = "헤더 프로필 이미지 조회",
      description = "현재 로그인한 사용자의 프로필 이미지를 조회합니다. "
          + "fetch 사용 시 credentials: \"include\", "
          + "axios 사용 시 withCredentials: true 설정이 필요합니다. "
          + "Swagger UI에서는 먼저 /auth/login 호출 후 같은 브라우저 세션에서 실행해야 합니다."
  )

  public ResponseEntity<BaseResponse<HeaderProfileImgResponse>> getMyProfileImg(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    String profileImgUrl = userService.getProfileImg(userDetails);
    HeaderProfileImgResponse response = new HeaderProfileImgResponse(profileImgUrl);

      return ResponseEntity.ok()
//              .cacheControl(CacheControl.noCache().mustRevalidate())
              .body(BaseResponse.success(response));
  }
}
