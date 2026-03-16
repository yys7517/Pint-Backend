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
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "프로필 이미지 조회 성공",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": {
                          "profileImgUrl": "https://example.com/profile.png"
                        }
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "200",
          description = "프로필 이미지 없을 때",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": {
                          "profileImgUrl": ""
                        }
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "로그인되지 않은 요청",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 401,
                        "message": "로그인이 필요합니다.",
                        "data": null
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "403",
          description = "세션 쿠키 누락 또는 인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 403,
                        "message": "접근 권한이 없습니다.",
                        "data": null
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "404",
          description = "사용자 조회 실패",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 404,
                        "message": "사용자를 찾을 수 없습니다: user@example.com",
                        "data": null
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 500,
                        "message": "서버 내부 오류가 발생했습니다.",
                        "data": null
                      }
                      """
              )
          )
      )
  })
  public ResponseEntity<BaseResponse<HeaderProfileImgResponse>> getMyProfileImg(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    String profileImgUrl = userService.getProfileImg(userDetails);
    HeaderProfileImgResponse response = new HeaderProfileImgResponse(profileImgUrl);

      return ResponseEntity.ok()
              .cacheControl(CacheControl.noCache().mustRevalidate())
              .body(BaseResponse.success(response));
  }
}
