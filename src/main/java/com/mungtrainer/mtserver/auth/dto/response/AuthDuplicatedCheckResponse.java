package com.mungtrainer.mtserver.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class AuthDuplicatedCheckResponse {
  private boolean isValid;
  private String message;
}
