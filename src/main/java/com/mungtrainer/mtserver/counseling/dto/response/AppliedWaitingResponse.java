package com.mungtrainer.mtserver.counseling.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppliedWaitingResponse {
	private Long applicationId;
	private String dogName;
	private String ownerName;
	private Long userId;
	private String fcmToken;
	private String courseTitle;
	private LocalDate sessionDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private String status; // APPLIED, WAITING (상담 완료 후 상태만 포함)

	// WAITING 상태 추가 정보
	private Boolean isWaiting; // WAITING 상태 여부 (편의성)
	private Boolean isPreApproved; // 미리 승인 여부 (waiting.is_approved)
	private Integer waitingOrder; // 대기 순번 (1부터 시작, null이면 대기 중 아님)
}
