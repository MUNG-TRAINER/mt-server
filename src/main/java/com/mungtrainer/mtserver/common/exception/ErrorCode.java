package com.mungtrainer.mtserver.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 회원 관련
  INVALID_REGIST_CODE(400,"등록번호가 존재하지 않습니다"),

  // 출석 관련
  ATTENDANCE_UPDATE_FAILED(500,"출석 상태 변경에 실패했습니다");

  public final int status;
  public final String message;
}
