package com.mungtrainer.mtserver.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 필드를 담는 기본 엔티티 클래스
 * - 생성 정보: createdBy, createdAt
 * - 수정 정보: updatedBy, updatedAt
 * - 삭제 정보(Soft Delete): isDeleted, deletedAt
 */
@Getter
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
    private boolean isDeleted = false;

    /**
     * 삭제 일시
     */
    private LocalDateTime deletedAt;

    public void markAsDeleted(Long deletedBy) {
      this.isDeleted = true;
      this.deletedAt = LocalDateTime.now();
      this.updatedBy = deletedBy;
      this.updatedAt = LocalDateTime.now();
    }
}