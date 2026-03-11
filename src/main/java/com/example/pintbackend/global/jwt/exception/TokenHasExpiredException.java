package com.example.pintbackend.global.jwt.exception;

public class TokenHasExpiredException extends RuntimeException {

  public TokenHasExpiredException(String message) {
    super(message);
  }
}
