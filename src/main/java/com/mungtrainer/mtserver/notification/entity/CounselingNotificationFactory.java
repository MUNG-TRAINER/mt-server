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

    /**
     * (회원 ← 훈련사) 상담 내용 작성 완료 알림
     */
    public NotificationCommand counselingCompleted(
            Long userId,
            Long counselingId,
            Long memberId
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)                 // 알림 대상: 회원
                .type("COUNSELING_COMPLETED")
                .title("상담이 완료되었습니다")
                .message("훈련사가 상담 내용을 작성했습니다. 확인해보세요.")
                .referenceId(counselingId)
                .referenceType("COUNSELING")
                .actionUrl("/counseling/" + counselingId)
                .actorId(memberId)                     // 행동 주체: 훈련사
                .build();
    }


}