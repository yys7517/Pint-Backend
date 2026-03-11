package com.example.pintbackend.service;

import com.example.pintbackend.domain.user.entity.User;
import com.example.pintbackend.domain.user.entity.Role;
import com.example.pintbackend.domain.user.exception.DuplicateEmailException;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  // 회원가입 - 기본 제공 save 사용
  @Transactional
  public void signupUser(User user) {
    if (userRepository.existsByEmail(user.getEmail())) {
      throw new DuplicateEmailException(user.getEmail());
    }

    // password encoding
    user.setRole(Role.USER);

    userRepository.save(user);
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
