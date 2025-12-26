package com.mungtrainer.mtserver.notification.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * SSE 클라이언트 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationSseClient extends BaseEntity {

    /**
     * SSE 클라이언트 ID (PK)
     */
    private Long sseClientId;

    /**
     * 사용자 ID (FK - user.user_id)
     */
    private Long userId;

    /**
     * 마지막 이벤트 ID
     */
    private Long lastEventId;

    /**
     * 활성화 여부
     */
    private Boolean isActive;

    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}

