package com.mungtrainer.mtserver.notification.entity;

import org.springframework.stereotype.Component;

@Component
public class TrainingNotificationFactory {

    /**
     * (훈련사 ← 회원) 훈련 과정 신청 알림
     */
    public NotificationCommand trainingRequest(
            Long trainerId,    // 알림 대상: 훈련사
            Long trainingId,   // 신청한 훈련 과정 ID
            Long memberId      // 행동 주체: 회원
    ) {
        return NotificationCommand.builder()
                .targetUserId(trainerId)
                .type("TRAINING_REQUEST")
                .title("새 훈련 과정 신청")
                .message("회원이 훈련 과정을 신청했습니다. 확인해보세요.")
                .referenceId(trainingId)
                .referenceType("TRAINING")
                .actionUrl("/trainer/training/" + trainingId) // 훈련사에서 확인할 URL
                .actorId(memberId)
                .build();
    }

    /**
     * (회원 ← 훈련사) 훈련 과정 승인/완료 알림
     */
    public NotificationCommand trainingApproved(
            Long userId,      // 알림 대상: 회원
            Long trainingId,  // 훈련 과정 ID
            Long trainerId    // 행동 주체: 훈련사
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("TRAINING_APPROVED")
                .title("훈련 과정이 승인되었습니다")
                .message("훈련사가 훈련 과정을 승인했습니다. 확인해보세요.")
                .referenceId(trainingId)
                .referenceType("TRAINING")
                .actionUrl("/training/" + trainingId)
                .actorId(trainerId)
                .build();
    }
}