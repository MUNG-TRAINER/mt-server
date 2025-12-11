package com.mungtrainer.mtserver.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 회원 관련
  INVALID_REGIST_CODE(400,"등록번호가 존재하지 않습니다"),
  USER_USERNAME_DUPLICATE(400, "이미 사용 중인 아이디입니다."),
  USER_EMAIL_DUPLICATE(400, "이미 사용 중인 이메일입니다."),
  LOGIN_FAILED(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
  NOT_FOUND_USERNAME(500, "해당 유저는 존재하지 않습니다."),
  INVALID_OLD_PASSWORD(400, "비밀번호 변경에 실패했습니다."),
  INVALID_CONFIRM_PASSWORD(400, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
  USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
  PROFILE_UPDATE_FAILED(500, "프로필 수정에 실패했습니다."),

  // 출석 관련
  ATTENDANCE_UPDATE_FAILED(500,"출석 상태 변경에 실패했습니다");

  public final int status;
  public final String message;
}
