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
  NOT_FOUND_USERNAME(400, "해당 유저는 존재하지 않습니다."),
  INVALID_OLD_PASSWORD(400, "이전 비밀번호 확인이 실패했습니다."),
  INVALID_CONFIRM_PASSWORD(400, "비밀번호 확인이 실패했습니다."),
  USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
  PROFILE_UPDATE_FAILED(500, "프로필 수정에 실패했습니다."),
  INVALID_COURSE_ID(400, "유효하지 않은 훈련 과정 ID입니다."),

  // 훈련 과정
  INTERNAL_SERVER_ERROR(500, "고유 tags 생성에 실패했습니다."),

  // 신청 관련
  DUPLICATE_APPLICATION(409, "이미 신청한 세션입니다.(APPLIED 상태)"),
  APPLICATION_NOT_FOUND(404, "신청 정보를 찾을 수 없습니다."),
  UNAUTHORIZED_APPLICATION(403, "본인만 신청 가능합니다."),
  APPLICATION_CREATION_FAILED(500, "신청 생성 실패"),

  // 출석 관련
  ATTENDANCE_UPDATE_FAILED(500,"출석 상태 변경에 실패했습니다"),

  // 반려견 관련
  DOG_NOT_FOUND(404, "존재하지 않는 반려견입니다."),
  DOG_NO_PERMISSION(403, "해당 반려견에 대한 권한이 없습니다."),
  DOG_NAME_DUPLICATE(400, "이미 등록된 반려견 이름입니다."),
  DOG_CREATE_FAILED(500, "반려견 등록에 실패했습니다."),
  DOG_UPDATE_FAILED(500, "반려견 정보 수정에 실패했습니다."),
  DOG_DELETE_FAILED(500, "반려견 삭제에 실패했습니다."),
  DOG_IMAGE_DELETE_FAILED(500, "반려견 프로필 이미지 삭제에 실패했습니다."),
  DOG_ID_GENERATION_FAILED(500, "반려견 ID 생성에 실패했습니다."),

  // 훈련 세션 관련
  SESSION_NOT_FOUND(404, "해당 세션을 찾을 수 없습니다."),
  SESSION_NO_PERMISSION(403, "해당 세션에 대한 권한이 없습니다."),
  SESSION_EMPTY_UPDATE(400, "수정할 내용이 없습니다."),
  SESSION_HAS_APPLICATIONS(400, "신청자가 있는 세션은 삭제할 수 없습니다."),
  SESSION_DELETE_FAILED(500, "세션 삭제에 실패했습니다."),
  SESSION_UPDATE_FAILED(500, "세션 수정에 실패했습니다."),

  // S3 관련
  INVALID_REQUEST_DATA(400, "요청 데이터가 유효하지 않습니다.");


  public final int status;
  public final String message;
}
