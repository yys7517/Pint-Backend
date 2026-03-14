package com.example.pintbackend.service;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.exception.DuplicateEmailException;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.postDto.profile.request.EditProfileRequest;
import com.example.pintbackend.dto.postDto.profile.response.EditProfileResponse;
import com.example.pintbackend.dto.postDto.profile.response.MyProfileResponse;
import com.example.pintbackend.dto.postDto.profile.response.ProfilePostResponse;
import com.example.pintbackend.dto.user.CustomUserDetails;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.dto.user.response.LoginUserResponse;
import com.example.pintbackend.global.exception.ForbiddenException;
import com.example.pintbackend.repository.PostLikeRepository;
import com.example.pintbackend.repository.UserRepository;
import com.example.pintbackend.service.s3service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostLikeRepository postLikeRepository;
    private final AuthenticationManager authenticationManager;
    private final S3Service s3Service;

  @Value("${server.servlet.session.cookie.secure:true}")
  private boolean secureCookie;

  @Value("${server.servlet.session.cookie.same-site:None}")
  private String sameSite;

  // 회원가입 - 기본 제공 save 사용
  @Transactional
  public void signupUser(User user) {
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new DuplicateEmailException(user.getEmail());
    }

    // password encoding
    user.encodePassword(passwordEncoder.encode(user.getPassword()));

    userRepository.save(user);
  }

  @Transactional
  public LoginUserResponse login(LoginUserRequest request, HttpServletRequest httpRequest) {
    // 1. 유저 인증 시도
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    // 인증 성공 시, Security에 컨텍스트에 유저 인증 정보 설정
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    // 세션 생성 및 세션 ID 설정
    HttpSession session = httpRequest.getSession(true);
    httpRequest.changeSessionId(); // 로그인 성공 후 세션 ID 교체

    session.setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
    );

    // 서블릿 컨테이너가 자동으로 쿠키에 "JSESSIONID", "XSRF-TOKEN"를 설정.
    // TODO. CSRF 나중에 구현
    // CsrfToken csrfToken = (CsrfToken) httpRequest.getAttribute(CsrfToken.class.getName());
    // String csrfTokenValue = csrfToken == null ? "" : csrfToken.getToken();

    Object principal = authentication.getPrincipal();
    Long userId = null;
    if (principal instanceof CustomUserDetails customUserDetails) {
      userId = customUserDetails.getUserId();
    } else {
      // 안전장치: principal 타입이 바뀌었을 때를 대비해 email(=principal name)로 조회
      userId = getUserId(authentication.getName());
    }

    return new LoginUserResponse(userId);
  }

  private Long getUserId(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new UserNotFoundException(email)
    );

    return user.getId();
  }

  public boolean isAvailableEmail(String email) {
    return !userRepository.existsByEmail(email);  // email을 쓰는 유저가 이미 존재하면 false값 반환
  }

  public List<ResponseCookie> signOut(HttpServletRequest request) {
    HttpSession session = request.getSession(false);  // 세션 가져오기
    if (session != null) session.invalidate();  // 세션 만료
    SecurityContextHolder.clearContext(); // Security 컨텍스트 비우기

    ResponseCookie deleteSessionCookie = ResponseCookie.from("JSESSIONID", "")  // 세션 ID 빈 값으로
        .httpOnly(true) // JS에서 접근 불가
        .secure(secureCookie)
        .sameSite(sameSite)
        .path("/")
        .maxAge(0)  // 세션 즉시 만료
        .build();

    ResponseCookie deleteCsrfCookie = ResponseCookie.from("XSRF-TOKEN", "") // XSRF-TOKEN 빈 값으로
        .httpOnly(false)
        .secure(secureCookie)
        .sameSite(sameSite)
        .path("/")
        .maxAge(0)
        .build();

    return List.of(deleteSessionCookie, deleteCsrfCookie);
  }

    public String getProfileImg(CustomUserDetails userDetails) {

        String profileImageKey = userDetails.getProfileImageS3Key();
        log.info("profileImageKey = {}", profileImageKey);
        String profileImgUrl = "";
        if (profileImageKey != null) {
            profileImgUrl = s3Service.getPresignedUrlToRead(profileImageKey);
        }
        return profileImgUrl;
    }

    /**
     * Transactional(readOnly=true) keeps hibernate session open
     */
    @Transactional(readOnly = true)
    public MyProfileResponse getProfile(Long targetUserid, CustomUserDetails userDetails) {

        // 유저 엔티티 불러오기
        User user = userRepository.findById(targetUserid)
                .orElseThrow(() -> new UserNotFoundException(userDetails.getEmail()));

        // 세션 유저가 자기 프로필 보고있으면 true
        boolean isMe = targetUserid.equals(userDetails.getUserId());

        log.info("isMe: {}", isMe);
        log.info("targetUserId: {}, sessionUserId: {}", targetUserid, userDetails.getUserId());

        String profileImageUrl = null;
        if(user.getProfileImageS3Key() != null) {
            profileImageUrl = s3Service.getPresignedUrlToRead(user.getProfileImageS3Key());
        }

        // target user 포스트 불러오기
        List<ProfilePostResponse> postList = user.getPosts().stream()
                .map(post -> ProfilePostResponse.from(
                        post,
                        s3Service.getPresignedUrlToRead(post.getCompressedImageFileS3Key() != null ? post.getCompressedImageFileS3Key() : post.getImageFileS3Key())
                ))
                .toList();

        // 내가 좋아한 포스트 불러오기 if isMe is true.
        List<ProfilePostResponse> likePostList = isMe
                ? postLikeRepository
                .findAllLikedPostByUserId(userDetails.getUserId())
                .stream()
                .map(post -> ProfilePostResponse.from(
                        post,
                        s3Service.getPresignedUrlToRead(post.getCompressedImageFileS3Key() != null ? post.getCompressedImageFileS3Key() : post.getImageFileS3Key())
                ))
                .toList()
                : List.of();

        return MyProfileResponse.from(user, profileImageUrl, isMe, postList, likePostList);
    }

    /**
     * 프로필 수정
     */
    public EditProfileResponse editProfile(Long targetUserId, CustomUserDetails userDetails,
                                           EditProfileRequest request) throws IOException {
        // 세션 유저인지 확인하기
        if (!targetUserId.equals(userDetails.getUserId())) {
            throw new ForbiddenException("본인의 프로필만 수정가능함 (에초에 버튼이 안보여야 정상!)");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(targetUserId)));

        // 새로운 이미지가 생성됬으면 이전 사진 S3 에서 지우기
        String profileImageS3Key = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            if (user.getProfileImageS3Key() != null) {
                s3Service.deletePost(user.getProfileImageS3Key());
            }
            profileImageS3Key = s3Service.uploadFile(request.getProfileImage());
        }

        // null fields are ignored by update() - only provided fields are changed
        user.update(request.getUsername(), request.getIntroduction(), request.getCity(), profileImageS3Key);

        // after update(), generate preignedURL
        String profileImageUrl = user.getProfileImageS3Key() != null
                ? s3Service.getPresignedUrlToRead(user.getProfileImageS3Key())
                : "";

        return EditProfileResponse.from(user, profileImageUrl);

    }


}
