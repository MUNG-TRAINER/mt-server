package com.mungtrainer.mtserver.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthJoinResponse {
  private Long userId;
  private String userName;
  private String email;
}
