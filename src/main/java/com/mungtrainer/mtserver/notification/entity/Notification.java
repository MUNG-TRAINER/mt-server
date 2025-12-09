package com.mungtrainer.mtserver.notification.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    /**
     * 알림 ID (PK)
     */
    private Long notificationId;

    /**
     * 대상 사용자 ID (FK - user.user_id)
     */
    private Long targetUserId;

    /**
     * 알림 유형 (SESSION_CHANGE, FEEDBACK, PAYMENT, NOTICE, SYSTEM)
     */
    private String type;

    /**
     * 제목
     */
    private String title;

    /**
     * 메시지
     */
    private String message;

    /**
     * 참조 ID (참조하는 엔티티의 ID)
     */
    private Long referenceId;

    /**
     * 참조 타입 (참조한 엔티티의 테이블 이름)
     */
    private String referenceType;

    /**
     * 읽음 여부
     */
    private Boolean isRead;

    /**
     * 액션 URL
     */
    private String actionUrl;

    /**
     * 예약 발송 시간
     */
    private LocalDateTime sendAt;
}

