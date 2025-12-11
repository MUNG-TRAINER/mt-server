package com.mungtrainer.mtserver.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode code;

  public CustomException(ErrorCode code) {
    super(code.getMessage());
    this.code = code;
  }
}
