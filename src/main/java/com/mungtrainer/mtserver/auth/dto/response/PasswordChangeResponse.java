package com.mungtrainer.mtserver.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordChangeResponse {
  private String status;
  private int code;
  private String message;
}
