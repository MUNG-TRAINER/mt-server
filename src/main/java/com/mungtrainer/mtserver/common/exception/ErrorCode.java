package com.mungtrainer.mtserver.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 토큰 관련
  TOKEN_EXPIRED(401,"Access 토큰이 만료되었습니다."),
  UNAUTHORIZED(401, "토큰이 유효하지 않거나 만료되었습니다."),
  REFRESH_EXPIRED(401, "Refresh 토큰이 만료되었습니다."),

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
  PROFILE_NOT_PUBLIC(403, "해당 사용자의 프로필은 비공개입니다."),
  INVALID_COURSE_ID(400, "유효하지 않은 훈련 과정 ID입니다."),


  // 훈련 과정
  INTERNAL_SERVER_ERROR(500, "고유 tags 생성에 실패했습니다."),
  TRAINER_NOT_FOUND(404, "연결된 훈련사를 찾을 수 없습니다."),
  INVALID_LESSON_FORM(400, "훈련 형태가 유효하지 않습니다. 허용된 값: WALK, GROUP, PRIVATE"),
  INVALID_DATE_RANGE(400, "시작 날짜는 종료 날짜보다 이전이어야 합니다."),

  // 신청 관련
  DUPLICATE_APPLICATION(409, "이미 신청한 세션입니다."),
  APPLICATION_NOT_FOUND(404, "신청 정보를 찾을 수 없습니다."),
  UNAUTHORIZED_APPLICATION(403, "본인만 신청 가능합니다."),
  APPLICATION_CREATION_FAILED(500, "신청 생성 실패"),
  COUNSELING_REQUIRED(409, "반려견의 상담이 필요합니다."),
  SESSION_DEADLINE_PASSED(400, "수업 시작 마감 시간이 지났습니다."),

  // 신청 승인/거절 관련
  APPLICATION_STATUS_REQUEST_EMPTY(400, "요청 데이터가 존재하지 않습니다."),
  APPLICATION_STATUS_REQUIRED(400, "status 값은 필수입니다."),
  APPLICATION_STATUS_INVALID(400, "잘못된 status 값입니다. (ACCEPT 또는 REJECTED)"),
  APPLICATION_REJECT_REASON_REQUIRED(400, "거절 사유(rejectReason)는 필수입니다."),
  APPLICATION_ALREADY_PROCESSED(400, "APPLIED 상태일 때만 승인/거절이 가능합니다."),
  APPLICATION_NO_MATCHING_RECORD(404, "승인/거절할 수 있는 신청이 없습니다. (이미 처리되었거나 존재하지 않는 신청입니다.)"),
  APPLICATION_DETAIL_NOT_FOUND(404, "해당 신청 정보를 찾을 수 없거나 접근 권한이 없습니다."),

  // 코스 상세페이지
  COURSE_NOT_FOUND(404, "존재하지 않는 훈련과정입니다."),

  // 출석 관련
  ATTENDANCE_UPDATE_FAILED(500,"출석 상태 변경에 실패했습니다"),
  ATTENDANCE_CREATION_FAILED(500, "출석 정보 생성에 실패했습니다"),

  // 반려견 관련

  DOG_NOT_FOUND(404, "존재하지 않는 반려견입니다."),
  DOG_NO_PERMISSION(403, "해당 반려견에 대한 권한이 없습니다."),
  DOG_NAME_DUPLICATE(400, "이미 등록된 반려견 이름입니다."),
  DOG_CREATE_FAILED(500, "반려견 등록에 실패했습니다."),
  DOG_UPDATE_FAILED(500, "반려견 정보 수정에 실패했습니다."),
  DOG_DELETE_FAILED(500, "반려견 삭제에 실패했습니다."),
  DOG_IMAGE_DELETE_FAILED(500, "반려견 프로필 이미지 삭제에 실패했습니다."),
  DOG_ID_GENERATION_FAILED(500, "반려견 ID 생성에 실패했습니다."),
  DOG_HAS_ACTIVE_TRAINING(400, "진행 중인 훈련 과정이 있어 삭제할 수 없습니다."),

  // 훈련 과정
  UNAUTHORIZED_RESOURCE_ACCESS(403, "본인이 작성한 리소스가 아닙니다."),
  COURSE_HAS_PAID_APPLICATIONS(400, "해당 코스에는 환불되지 않은 신청 내역이 있어 삭제할 수 없습니다. 먼저 환불을 처리한 후 다시 시도해주세요."),

  // ===== 주문/결제 관련 =====
  ORDER_NOT_FOUND(404, "주문 정보를 찾을 수 없습니다."),
  ORDER_CREATION_FAILED(500, "주문 생성에 실패했습니다."),
  PAYMENT_INVALID_AMOUNT(400, "결제 금액이 유효하지 않습니다."),
  PAYMENT_FAILED(500, "결제 처리에 실패했습니다."),
  PAYMENT_ALREADY_COMPLETED(409, "이미 결제가 완료된 주문입니다."),
  SESSION_CANNOT_DELETE_HAS_PAYMENT(400, "결제 완료된 신청이 있습니다. 환불 처리 후 삭제해주세요."),
  PAYMENT_AMOUNT_MISMATCH(400, "결제 금액이 일치하지 않습니다."),
  PAYMENT_APPROVAL_FAILED(500, "결제 승인에 실패했습니다."),
  PAYMENT_NOT_FOUND(404, "결제 정보를 찾을 수 없습니다."),
  PAYMENT_CANCEL_FAILED(500, "결제 취소에 실패했습니다."),

    // 장바구니(Wishlist) 관련
    WISHLIST_NOT_FOUND(404, "장바구니 항목을 찾을 수 없습니다"),
    UNAUTHORIZED_WISHLIST(403, "권한이 없는 장바구니 항목입니다"),
    INVALID_WISHLIST_STATUS(400, "장바구니 상태가 유효하지 않습니다"),
    COURSE_PRICE_NOT_FOUND(404, "코스 가격 정보를 찾을 수 없습니다"),
    COURSE_DUPLICATE(400, "이미 장바구니에 담긴 코스입니다."),

  // 훈련 세션 관련
  SESSION_NOT_FOUND(404, "해당 세션을 찾을 수 없습니다."),
  SESSION_NO_PERMISSION(403, "해당 세션에 대한 권한이 없습니다."),
  SESSION_EMPTY_UPDATE(400, "수정할 내용이 없습니다."),
  SESSION_HAS_APPLICATIONS(400, "신청자가 있는 세션은 삭제할 수 없습니다."),
  SESSION_DELETE_FAILED(500, "세션 삭제에 실패했습니다."),
  SESSION_UPDATE_FAILED(500, "세션 수정에 실패했습니다."),

  // S3 관련
  INVALID_REQUEST_DATA(400, "요청 데이터가 유효하지 않습니다."),

  // 상담 관련
  COUNSELING_DOG_ID_REQUIRED(400, "반려견 ID는 필수입니다."),
  COUNSELING_DOG_NOT_OWNED(403, "존재하지 않는 반려견이거나 접근 권한이 없습니다."),
  COUNSELING_INVALID_PHONE(400, "전화번호 형식이 올바르지 않습니다."),
  COUNSELING_CREATE_FAILED(500, "상담 신청에 실패했습니다."),
  COUNSELING_NOT_FOUND(404, "존재하지 않는 상담입니다."),
  COUNSELING_ALREADY_DELETED(404, "이미 취소된 상담이거나 존재하지 않는 상담입니다."),
  COUNSELING_NO_PERMISSION(403, "해당 상담을 취소할 권한이 없습니다."),
  COUNSELING_CANCEL_FAILED(500, "상담 취소에 실패했습니다."),
  COUNSELING_ALREADY_COMPLETED(409, "이미 완료된 상담입니다."),
  COUNSELING_UPDATE_FAILED(500, "상담 내용 저장에 실패했습니다."),
  COUNSELING_DETAIL_NOT_FOUND(404, "존재하지 않는 상담이거나 접근 권한이 없습니다."),
  COUNSELING_TRAINER_NO_PERMISSION(403, "해당 상담에 접근할 권한이 없습니다. 담당 고객의 상담만 처리할 수 있습니다.");


  public final int status;
  public final String message;
}
