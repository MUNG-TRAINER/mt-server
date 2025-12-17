package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 훈련과정 검색 결과 응답 DTO
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
     * 전체 결과 개수
     */
    private Integer totalCount;

    /**
     * 현재 페이지 번호
     */
    private Integer currentPage;

    /**
     * 전체 페이지 수
     */
    private Integer totalPages;

    /**
     * 페이지당 항목 수
     */
    private Integer pageSize;
}

