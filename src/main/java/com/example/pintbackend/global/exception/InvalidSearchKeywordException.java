package com.example.pintbackend.global.exception;

public class InvalidSearchKeywordException extends RuntimeException {

  public InvalidSearchKeywordException() {
    super("검색어를 입력해주세요.");
  }
}
