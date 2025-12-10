package com.mungtrainer.mtserver.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class AuthUserJoinRequest {

  @NotBlank(message = "아이디는 필수입니다.")
  @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
  private String userName;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotNull(message = "생년월일은 필수입니다.")
  private LocalDate birth;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  private String password;

  // 01011112222, 0113335555 형식만 통과
  @Pattern(regexp = "^[0-9]{10,11}$", message = "전화번호 형식이 올바르지 않습니다.")
  private String phone;

  private Boolean isPublic;
  private Boolean isAgree;
  private String sido;
  private String sigungu;
  private String roadname;
  private String restAddress;
  private String postcode;

  // 가입코드
  private String registCode;

}
