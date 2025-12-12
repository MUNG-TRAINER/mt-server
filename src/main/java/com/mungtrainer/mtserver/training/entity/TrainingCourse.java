package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 훈련 코스 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingCourse extends BaseEntity {

    /**
     * 코스 ID (PK)
     */
    private Long courseId;

    /**
     * 훈련사 ID (FK - user.user_id)
     */
    private Long trainerId;

    /**
     * 태그
     */
    private String tags;

    /**
     * 제목
     */
    private String title;

    /**
     * 설명
     */
    private String description;

    /**
     * 회차 여부
     */
    @Pattern(regexp = "산책|그룹수업|1:1", message = "유효하지 않은 상태입니다.")
    private String type;

   /**
   * 수업 유형: WALK, GROUP, PRIVATE
   */
    private String lessonForm;

    /**
     * 상태 (SCHEDULED, CANCELLED, DONE)
     */
    @Pattern(regexp = "SCHEDULED|CANCELLED|DONE", message = "유효하지 않은 상태입니다.")
    private String status;

    /**
     * 무료 여부
     */
    private Boolean isFree;

    /**
     * 난이도
     */
    @Pattern(regexp = "초급|중급|고급", message = "유효하지 않은 상태입니다.")
    private String difficulty;

    /**
     * 위치 (주소 문자열)
     */
    private String location;

    /**
     * 일정
     */
    private String schedule;

    /**
     * 환불 정책
     */
    private String refundPolicy;

    /**
     * 메인 이미지
     */
    private String mainImage;

    /**
     * 상세 이미지
     */
    private String detailImage;

    /**
     * 준비물
     */
    private String items;

    /**
     * 반려견 크기
     */
    @Pattern(regexp = "소형견|중형견|대형견", message = "유효하지 않은 상태입니다.")
    private String dogSize;
}

