package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 훈련과정 검색 결과 아이템 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSearchItemDto {

    /**
     * 훈련과정 ID
     */
    private Long courseId;

    /**
     * 훈련사 ID
     */
    private Long trainerId;

    /**
     * 훈련사 이름
     */
    private String trainerName;

    /**
     * 제목
     */
    private String title;

    /**
     * 설명
     */
    private String description;

    /**
     * 태그
     */
    private String tags;

    /**
     * 메인 이미지 URL (Presigned URL)
     */
    private String mainImage;

    /**
     * 훈련 유형 (ONCE, MULTI)
     */
    private String type;

    /**
     * 수업 형태 (WALK, GROUP, PRIVATE 등)
     */
    private String lessonForm;

    /**
     * 상태 (SCHEDULED, CANCELLED, DONE)
     */
    private String status;

    /**
     * 난이도
     */
    private String difficulty;

    /**
     * 무료 여부
     */
    private Boolean isFree;

    /**
     * 위치 (시/도)
     */
    private String location;

    /**
     * 일정 정보
     */
    private String schedule;

    /**
     * 대상 강아지 크기
     */
    private String dogSize;

    /**
     * 세션 정보 (최저가 세션)
     */
    private SessionSummaryDto session;
}

