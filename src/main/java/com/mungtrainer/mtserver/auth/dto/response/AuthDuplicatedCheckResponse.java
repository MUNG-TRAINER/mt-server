package com.mungtrainer.mtserver.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthDuplicatedCheckResponse {
  boolean isValid;
  String message;
}
