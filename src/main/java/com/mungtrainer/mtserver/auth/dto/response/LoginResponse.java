package com.mungtrainer.mtserver.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
  private int status;
  private String code;
  private String message;
}
