package com.mungtrainer.mtserver.trainer.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 훈련사 프로필 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainerProfile extends BaseEntity {

    /**
     * 훈련사 ID (PK, FK - user.user_id)
     */
    private Long trainerId;

    /**
     * 경력 정보
     */
    private String careerInfo;

    /**
     * 소개
     */
    private String introduce;

    /**
     * 상세 설명
     */
    private String description;

    /**
     * 훈련 스타일
     */
    private String style;

    /**
     * 태그
     */
    private String tag;

    /**
     * 등록 코드
     */
    private String registCode;

    /**
     * 인기 훈련사 여부
     */
    private Boolean isPopular;

    /**
     * 자격증 이미지 URL
     */
    private String certificationImageUrl;
}

