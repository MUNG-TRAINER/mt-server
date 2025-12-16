package com.mungtrainer.mtserver.common.security;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthErrorResponse {
  private String code;
  private String message;
}
