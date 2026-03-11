package com.example.pintbackend.global.exception;

import com.example.pintbackend.domain.user.exception.DuplicateEmailException;
import com.example.pintbackend.domain.user.exception.UserNotFoundException;
import com.example.pintbackend.dto.common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 회원가입 이메일 중복
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<BaseResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(BaseResponse.fail(HttpStatus.CONFLICT.value(), e.getMessage()));
  }

  // 사용자 조회 실패
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(BaseResponse.fail(HttpStatus.NOT_FOUND.value(), e.getMessage()));
  }

  // BAD REQUEST
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(BaseResponse.fail(HttpStatus.BAD_REQUEST.value(), message));
  }

  // 중복 처리
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<BaseResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(BaseResponse.fail(HttpStatus.CONFLICT.value(), "중복된 값이 존재합니다."));
  }


  // 500 에러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleUnknown(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BaseResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다."));
  }
}
