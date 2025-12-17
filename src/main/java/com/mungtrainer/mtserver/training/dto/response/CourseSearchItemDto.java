package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
     * 수업 형태 (WALK, GROUP, PRIVATE 등)
     */
    private String lessonForm;

    /**
     * 난이도
     */
    private String difficulty;

    /**
     * 위치
     */
    private String location;

    /**
     * 훈련 유형 (ONCE, MULTI)
     */
    private String type;

    /**
     * 최소 가격
     */
    private Integer price;
}

