package com.mungtrainer.mtserver.notification.entity;

import org.springframework.stereotype.Component;

@Component
public class CounselingNotificationFactory {
    public NotificationCommand counselingRequest(
            Long trainerId,
            Long counselingId,
            Long memberId
    ) {
        return NotificationCommand.builder()
                .targetUserId(trainerId)
                .type("COUNSELING_REQUEST")
                .title("새 상담 신청")
                .message("반려견 상담 신청이 도착했습니다.")
                .referenceId(counselingId)
                .referenceType("COUNSELING")
                .actionUrl("/trainer/counseling/" + counselingId)
                .actorId(memberId)
                .build();
    }
}