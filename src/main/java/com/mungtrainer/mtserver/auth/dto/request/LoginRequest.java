package com.mungtrainer.mtserver.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
  private String userName;
  private String password;
}
