package com.mungtrainer.mtserver.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CheckResponse {
  private final Long userId;
  private final String username;
  private final String role;
}
