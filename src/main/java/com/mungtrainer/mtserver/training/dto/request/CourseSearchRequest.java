package com.mungtrainer.mtserver.training.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 훈련과정 검색 요청 DTO (무한 스크롤용 커서 기반 페이지네이션)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchRequest {

    /**
     * 검색 키워드 (제목 또는 태그로 검색)
     */
    private String keyword;

    /**
     * 마지막으로 조회한 courseId (커서)
     * null이면 첫 페이지 조회
     */
    private Long lastCourseId;

    /**
     * 조회할 항목 수
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 훈련사 ID (역할 기반 필터링용)
     * - USER: 자신이 속한 훈련사의 과정만 조회
     * - TRAINER: 자신이 등록한 과정만 조회
     */
    private Long trainerId;

    /**
     * 훈련 형태 필터 (선택적)
     * - WALK: 무료 산책모임
     * - GROUP: 그룹 레슨
     * - PRIVATE: 개인(1:1) 레슨
     */
    private String lessonForm;
}
