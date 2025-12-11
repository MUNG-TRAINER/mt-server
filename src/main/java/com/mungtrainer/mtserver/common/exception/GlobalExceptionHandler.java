package com.mungtrainer.mtserver.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();

    e.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<?> handleCustomException(CustomException e) {
    ErrorCode code = e.getCode();

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
}
