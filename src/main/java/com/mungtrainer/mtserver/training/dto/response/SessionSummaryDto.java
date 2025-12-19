package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 요약 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDto {

    /**
     * 세션 ID
     */
    private Long sessionId;

    /**
     * 시작 시간
     */
    private LocalDateTime startTime;

    /**
     * 종료 시간
     */
    private LocalDateTime endTime;

    /**
     * 상세 위치
     */
    private String locationDetail;

    /**
     * 최대 수강 인원
     */
    private Integer maxStudents;

    /**
     * 가격
     */
    private Integer price;
}

