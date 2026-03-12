package com.example.pintbackend.service;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.exception.DuplicateEmailException;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.user.request.LoginUserRequest;
import com.example.pintbackend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final AuthenticationManager authenticationManager;

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
  public Long login(LoginUserRequest request, HttpServletRequest httpRequest) {
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

    // 서블릿 컨테이너가 자동으로 쿠키에 "JSESSIONID"를 설정.

    return getUserId(request.email());
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

  public ResponseCookie signOut(HttpServletRequest request) {
    HttpSession session = request.getSession(false);  // 세션 가져오기
    if (session != null) session.invalidate();  // 세션 만료
    SecurityContextHolder.clearContext(); // Security 컨텍스트 비우기

    boolean isSecureRequest = request.isSecure();
    String sameSitePolicy = isSecureRequest ? "None" : "Lax";

    return ResponseCookie.from("JSESSIONID", "")  // 세션 ID 빈 값으로
        .httpOnly(true) // JS에서 접근 불가
        .secure(isSecureRequest)  //  HTTPS 관련 처리
        .sameSite(sameSitePolicy) //  HTTPS 관련 처리
        .path("/")
        .maxAge(0)  // 세션 즉시 만료
        .build();
  }
}
