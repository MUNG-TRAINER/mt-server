package com.mungtrainer.mtserver.notification.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 알림 로그 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationLog extends BaseEntity {

    /**
     * 로그 ID (PK)
     */
    private Long logId;

    /**
     * 알림 ID (FK - notification.notification_id)
     */
    private Long notificationId;

    /**
     * 채널 (PUSH, EMAIL, SMS)
     */
    private String channel;

    /**
     * 상태 (SENT, FAILED)
     */
    private String status;

    /**
     * 발송 일시
     */
    private LocalDateTime sentAt;
}

