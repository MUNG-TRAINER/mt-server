package com.mungtrainer.mtserver.counseling.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 상담 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Counseling extends BaseEntity {

    /**
     * 상담 ID (PK)
     */
    private Long counselingId;

    /**
     * 반려견 ID (FK - dog.dog_id)
     */
    private Long dogId;

    /**
     * 연락처
     */
    private String phone;

    /**
     * 상담 내용 (훈련사가 작성)
     */
    private String content;

    /**
     * 상담 완료 여부
     */
    private Boolean counselingYn;
}

