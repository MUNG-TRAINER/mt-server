package com.mungtrainer.mtserver.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
  private String oldPassword;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  private String newPassword;
  private String confirmPassword;
}
