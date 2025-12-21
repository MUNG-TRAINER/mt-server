package com.mungtrainer.mtserver.dog.dto.response;

import com.mungtrainer.mtserver.dog.entity.SocializationLevel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 반려견 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogResponse {

    /**
     * 반려견 ID
     */
    private Long dogId;

    /**
     * 반려견 이름
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
     * 성별 (M/F)
     */
    private String gender;

    /**
     * 중성화 여부
     */
    private Boolean isNeutered;

    /**
     * 몸무게
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
     * 프로필 이미지 URL
     */
    private String profileImage;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;
}