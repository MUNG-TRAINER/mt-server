package com.mungtrainer.mtserver.training.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 훈련과정 검색 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchRequest {

    /**
     * 검색 키워드 (제목으로 검색)
     */
    private String keyword;

    /**
     * 페이지 번호 (1부터 시작)
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 페이지당 항목 수
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 오프셋 계산
     */
    public Integer getOffset() {
        return (page - 1) * size;
    }
}

