package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 달력에 표시할 세션 날짜 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSessionDateDto {

    /**
     * 세션 날짜
     */
    private LocalDate sessionDate;

    /**
     * 해당 날짜의 세션 수
     */
    private Integer sessionCount;
}
