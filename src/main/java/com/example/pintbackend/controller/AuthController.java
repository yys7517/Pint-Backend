package com.example.pintbackend.controller;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.dto.user.response.CheckDuplicateEmailResponse;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.user.request.CreateUserRequest;
import com.example.pintbackend.dto.user.response.LoginUserResponse;
import com.example.pintbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
@Tag(name = "Auth", description = "회원 인증 API")
public class AuthController {

  private final UserService userService;

  @PostMapping("/signup")
  @Operation(
      summary = "회원가입",
      description = "이메일, 비밀번호, 닉네임으로 회원가입합니다."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "회원가입 성공",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": ""
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "필수값 누락",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 400,
                        "message": "이메일은 필수입니다.",
                        "data": null
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "409",
          description = "중복 이메일",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 409,
                        "message": "이미 사용 중인 이메일입니다: user@example.com",
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
  public ResponseEntity<BaseResponse<String>> signupUser(
      @Valid @RequestBody CreateUserRequest request
  ) {
    User user = request.toEntity();
    userService.signupUser(user);

    return ResponseEntity.ok(BaseResponse.success(""));
  }

  @PostMapping("/signout")
  @Operation(
      summary = "로그아웃",
      description = "현재 세션을 만료시키고 JSESSIONID 쿠키를 삭제합니다."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그아웃 성공",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": ""
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
  public ResponseEntity<BaseResponse<String>> logout(
      @Parameter(hidden = true) HttpServletRequest request
  ) {
    List<ResponseCookie> deleteCookies = userService.signOut(request);

    ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
    for (ResponseCookie cookie : deleteCookies) {
      responseBuilder.header(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    return responseBuilder.body(BaseResponse.success(""));
  }

  @PostMapping("/login")
  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인합니다. 성공하면 세션이 생성되고 JSESSIONID 쿠키가 발급됩니다."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": {
                          "csrfToken": "csrf-token-value"
                        }
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "400",
          description = "필수값 누락",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 400,
                        "message": "이메일은 필수입니다. or 비밀번호는 필수입니다.",
                        "data": null
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "401",
          description = "로그인 인증 실패",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 401,
                        "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
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
  public ResponseEntity<BaseResponse<LoginUserResponse>> login(
      @Valid @RequestBody LoginUserRequest request,
      @Parameter(hidden = true) HttpServletRequest httpRequest
  ) {
    LoginUserResponse response = userService.login(request, httpRequest);

    return ResponseEntity.ok(BaseResponse.success(response));
  }

  @PostMapping("/unique")
  @Operation(
      summary = "이메일 중복 확인",
      description = "입력한 이메일이 회원가입 가능한 이메일인지 확인합니다."
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "사용 가능한 이메일",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": {
                          "isAvailable": true
                        }
                      }
                      """
              )
          )
      ),
      @ApiResponse(
          responseCode = "201",
          description = "사용 불가능 이메일",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "code": 200,
                        "message": "Success",
                        "data": {
                          "isAvailable": false 
                        }
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
  public ResponseEntity<BaseResponse<CheckDuplicateEmailResponse>> checkDuplicateEmail(
      @Parameter(description = "이메일 중복 체크", example = "user@example.com")
      @RequestParam("email") String email
  ) {
    boolean isAvailable = userService.isAvailableEmail(email);
    CheckDuplicateEmailResponse response = new CheckDuplicateEmailResponse(isAvailable);

    return ResponseEntity.ok(BaseResponse.success(response));
  }
}
