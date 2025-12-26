package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 달력 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponse {

    /**
     * 세션이 있는 날짜 목록
     */
    private List<CalendarSessionDateDto> sessionDates;

    /**
     * 총 세션 날짜 수
     */
    private Integer totalDates;
}

