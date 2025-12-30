package com.mungtrainer.mtserver.counseling.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 코스별로 그룹핑된 승인 대기 목록 응답 DTO
 */
@Getter
@Setter
public class GroupedApplicationResponse {
	private Long courseId;
	private String courseTitle;
	private String courseType; // SINGLE, MULTI, GROUP
	private String lessonForm; // WALK, GROUP, PRIVATE
	private Long dogId;
	private String dogName;
	private String ownerName;
	private Long userId;
	private String fcmToken;
	private Integer totalSessions; // 해당 코스의 총 회차 수
	private List<SessionInfo> sessions; // 각 회차 정보

	@Getter
	@Setter
	public static class SessionInfo {
		private Long applicationId;
		private Long sessionId;
		private Integer sessionNo;
		private LocalDate sessionDate;
		private LocalTime startTime;
		private LocalTime endTime;
		private String status; // APPLIED, WAITING (상담 완료 후 상태만 포함)

		// WAITING 상태 추가 정보
		private Boolean isWaiting; // WAITING 상태 여부
		private Boolean isPreApproved; // 미리 승인 여부
		private Integer waitingOrder; // 대기 순번

		// 정원 정보
		private Integer maxCapacity; // 세션 최대 정원
		private Integer currentParticipants; // 현재 승인/결제 완료된 참여 인원 수 (ACCEPT, PAID)
		private Boolean isFull; // 정원 마감 여부
	}
}
