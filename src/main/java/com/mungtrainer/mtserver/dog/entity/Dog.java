package com.mungtrainer.mtserver.dog.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 반려견 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Dog extends BaseEntity {

    /**
     * 반려견 ID (PK)
     */
    private Long dogId;

    /**
     * 사용자 ID (FK - user.user_id)
     */
    private Long userId;

    /**
     * 이름 (user_id와 unique 조합)
     */
    private String name;

    /**
     * 품종
     */
    private String breed;

    /**
     * 나이
     */
    private Integer age;

    /**
     * 성별 (F, M)
     */
    private String gender;

    /**
     * 중성화 여부
     */
    private Boolean isNeutered;

    /**
     * 몸무게 (kg)
     */
    private BigDecimal weight;

    /**
     * 성격
     */
    private String personality;

    /**
     * 습관
     */
    private String habits;

    /**
     * 건강 정보
     */
    private String healthInfo;

    /**
     * 사람 사회화 수준
     */
    private SocializationLevel humanSocialization;

    /**
     * 동물 사회화 수준
     */
    private SocializationLevel animalSocialization;

    /**
     * 프로필 이미지
     */
    private String profileImage;
}

