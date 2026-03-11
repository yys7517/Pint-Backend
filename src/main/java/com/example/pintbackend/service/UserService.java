package com.example.pintbackend.service;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.entity.Role;
import com.example.pintbackend.domain.user.exception.DuplicateEmailException;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.global.jwt.dto.JwtTokenInfo;
import com.example.pintbackend.global.jwt.service.JwtTokenProvider;
import com.example.pintbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  // 회원가입 - 기본 제공 save 사용
  @Transactional
  public void signupUser(User user) {
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new DuplicateEmailException(user.getEmail());
    }

    // password encoding
    user.encodePassword(passwordEncoder.encode(user.getPassword()));
    user.setRole(Role.USER);

    userRepository.save(user);
  }

  @Transactional
  public JwtTokenInfo signin(String email, String password) {
    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
    Authentication authentication;
    try {
      authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.", e);
    }
    JwtTokenInfo jwtToken;
    try {
      jwtToken = jwtTokenProvider.generateToken(authentication);
    } catch (Exception e) {
      log.error("JWT generation failed. email={}", email, e);
      throw e;
    }

    return jwtToken;
  }

  public Long getUserId(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new UserNotFoundException(email)
    );

    return user.getId();
  }

  public boolean isAvailableEmail(String email) {
    return !userRepository.existsByEmail(email);  // email을 쓰는 유저가 이미 존재하면 false값 반환
  }
}
