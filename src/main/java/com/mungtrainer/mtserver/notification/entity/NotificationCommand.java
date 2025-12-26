package com.mungtrainer.mtserver.notification.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 범용 알림 발송 메서드
public class NotificationCommand {

    private Long targetUserId;   // 수신자
    private String type;         // 알림 타입
    private String title;
    private String message;

    private Long referenceId;
    private String referenceType;
    private String actionUrl;

    private Long actorId;        // 행위자 (createdBy)
}
