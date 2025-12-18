package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 훈련과정 검색 결과 응답 DTO (무한 스크롤용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchResponse {

    /**
     * 검색 결과 목록
     */
    private List<CourseSearchItemDto> courses;

    /**
     * 다음 페이지 존재 여부
     */
    private Boolean hasMore;

    /**
     * 마지막 courseId (다음 요청 시 사용할 커서)
     */
    private Long lastCourseId;

    /**
     * 현재 조회된 항목 수
     */
    private Integer size;
}

