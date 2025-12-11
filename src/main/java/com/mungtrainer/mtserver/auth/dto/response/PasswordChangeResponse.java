package com.mungtrainer.mtserver.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordChangeResponse {
  private String message;
}
