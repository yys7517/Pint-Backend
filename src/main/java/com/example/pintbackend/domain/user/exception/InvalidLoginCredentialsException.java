package com.example.pintbackend.domain.user.exception;

public class InvalidLoginCredentialsException extends RuntimeException {

  public InvalidLoginCredentialsException() {
    super("이메일 또는 비밀번호가 올바르지 않습니다.");
  }
}
