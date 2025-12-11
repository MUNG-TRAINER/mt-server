package com.mungtrainer.mtserver.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 커스텀
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<?> handleCustomException(CustomException e, HttpServletRequest request) {
    ErrorCode code = e.getCode();

    log.warn("Business error at {} - code: {}, msg: {}",
             request.getRequestURI(),
             e.getCode(),
             e.getMessage());

    ErrorResponse response = ErrorResponse.builder()
        .status(code.getStatus())
        .error(code.name())
        .message(code.getMessage())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity
        .status(code.getStatus())
        .body(response);
  }

  // 400 입력값 validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();

    log.warn("Validation error at {} - code: {}, msg: {}",
             request.getRequestURI(),
             e.getStatusCode(),
             e.getMessage());

    e.getBindingResult()
     .getFieldErrors()
     .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }

  // 401 로그인 실패
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
    ErrorCode code = ErrorCode.LOGIN_FALIED;

    ErrorResponse response = ErrorResponse.builder()
                                          .status(code.getStatus())
                                          .error(code.name())
                                          .message(code.getMessage())
                                          .timestamp(LocalDateTime.now())
                                          .build();

    return ResponseEntity
        .status(code.getStatus())
        .body(response);
  }

  // 404 요청 api 없음
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
    ErrorResponse response = ErrorResponse.builder()
        .status(404)
        .error("NOT_FOUND")
        .message("요청한 URL을 찾을 수 없습니다." + e.getRequestURL())
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity
        .status(404)
        .body(response);
  }

  // 500 이외의 예외들
  // TODO: 배포 시 활성화
//  @ExceptionHandler(Exception.class)
//  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
//    ErrorResponse response = ErrorResponse.builder()
//                                          .status(500)
//                                          .error("INTERNAL_ERROR")
//                                          .message(ex.getMessage())
//                                          .timestamp(LocalDateTime.now())
//                                          .build();
//    return ResponseEntity.internalServerError().body(response);
//  }

}
