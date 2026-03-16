package com.example.pintbackend.controller;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.dto.GetLoginPagePostResponse;
import com.example.pintbackend.dto.LoginPagePostResponse;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.dto.user.response.CheckDuplicateEmailResponse;
import com.example.pintbackend.dto.common.response.BaseResponse;
import com.example.pintbackend.dto.user.request.CreateUserRequest;
import com.example.pintbackend.dto.user.response.LoginUserResponse;
import com.example.pintbackend.service.PostService;
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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final PostService postService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원가입합니다."
    )
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
    public ResponseEntity<BaseResponse<CheckDuplicateEmailResponse>> checkDuplicateEmail(
            @Parameter(description = "이메일 중복 체크", example = "user@example.com")
            @RequestParam("email") String email
    ) {
        boolean isAvailable = userService.isAvailableEmail(email);
        CheckDuplicateEmailResponse response = new CheckDuplicateEmailResponse(isAvailable);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/posts")
    public ResponseEntity<BaseResponse<GetLoginPagePostResponse>> getLoginPagePosts() {
        List<LoginPagePostResponse> postList = postService.getLoginPagePosts();
        GetLoginPagePostResponse response = new GetLoginPagePostResponse(postList);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(BaseResponse.success(response));
    }
}
