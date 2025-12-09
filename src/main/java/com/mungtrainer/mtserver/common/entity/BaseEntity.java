package com.mungtrainer.mtserver.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 필드를 담는 기본 엔티티 클래스
 * created_by, created_at, updated_by, updated_at 필드 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    /**
     * 생성자 ID
     */
    private Long createdBy;

    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정자 ID
     */
    private Long updatedBy;

    /**
     * 수정 일시
     */
    private LocalDateTime updatedAt;

    /**
     * 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 삭제 일시
     */
    private LocalDateTime deletedAt;
}