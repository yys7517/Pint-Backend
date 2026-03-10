package com.example.pintbackend.dto.common.response;

/**
 * 모든 API 응답에서 공통으로 사용하는 기본 응답 포맷.
 *
 * @param <T> 실제 payload 타입
 */
public record BaseResponse<T>(
    int code,
    String message,
    T data
) {
  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(200, "Success", data);
  }

  public static <T> BaseResponse<T> fail(int code, String message) {
    return new BaseResponse<>(code, message, null);
  }

  public static <T> BaseResponse<T> of(int code, String message, T data) {
    return new BaseResponse<>(code, message, data);
  }
}



