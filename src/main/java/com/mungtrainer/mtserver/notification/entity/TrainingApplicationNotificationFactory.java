package com.mungtrainer.mtserver.notification.entity;

import org.springframework.stereotype.Component;

/**
 * 훈련 신청 관련 알림 팩토리
 */
@Component
public class TrainingApplicationNotificationFactory {

    /**
     * 승인 완료 (대기열 진입) 알림
     * 트레이너가 승인했으나 정원이 마감되어 대기 중
     */
    public NotificationCommand approvalWithWaiting(
            Long userId,
            Long applicationId,
            Long trainerId
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("APPLICATION_WAITING")
                .title("승인 완료 (대기)")
                .message("트레이너가 승인했으나 정원이 마감되어 대기 중입니다. 자리가 생기면 자동으로 결제 가능합니다.")
                .referenceId(applicationId)
                .referenceType("TRAINING_APPLICATION")
                .actionUrl("/training/application/" + applicationId)
                .actorId(trainerId)
                .build();
    }

    /**
     * 승인 완료 알림
     * 정원 여유가 있어 바로 승인됨
     */
    public NotificationCommand approvalCompleted(
            Long userId,
            Long applicationId,
            Long trainerId
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("APPLICATION_APPROVED")
                .title("승인 완료")
                .message("훈련 신청이 승인되었습니다! 결제를 진행해주세요.")
                .referenceId(applicationId)
                .referenceType("TRAINING_APPLICATION")
                .actionUrl("/payments/" + applicationId)
                .actorId(trainerId)
                .build();
    }

    /**
     * 대기자 자동 승격 알림
     * 대기 상태에서 자동으로 승인 완료됨
     */
    public NotificationCommand waitingPromoted(
            Long userId,
            Long applicationId,
            int paymentDeadlineHours
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("APPLICATION_PROMOTED")
                .title("승인 완료")
                .message(String.format("대기가 해제되어 자동으로 승인 완료되었습니다! %d시간 내에 결제를 진행해주세요.", paymentDeadlineHours))
                .referenceId(applicationId)
                .referenceType("TRAINING_APPLICATION")
                .actionUrl("/payments/" + applicationId)
                .actorId(0L) // 시스템 자동 처리
                .build();
    }

    /**
     * 결제 기한 만료 알림
     * 결제를 하지 않아 신청이 만료됨
     */
    public NotificationCommand paymentExpired(
            Long userId,
            Long applicationId
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("PAYMENT_EXPIRED")
                .title("결제 기한 만료")
                .message("결제 기한이 지나 신청이 만료되었습니다. 다시 신청해주세요.")
                .referenceId(applicationId)
                .referenceType("TRAINING_APPLICATION")
                .actionUrl("/training/application/" + applicationId)
                .actorId(0L) // 시스템 자동 처리
                .build();
    }

    /**
     * 세션 시작 마감 알림
     * 수업 시작 시간이 임박하여 신청이 마감됨
     */
    public NotificationCommand sessionDeadlineExpired(
            Long userId,
            Long applicationId
    ) {
        return NotificationCommand.builder()
                .targetUserId(userId)
                .type("SESSION_DEADLINE_EXPIRED")
                .title("수업 시작으로 신청 마감")
                .message("수업이 시작되어 신청이 마감되었습니다.")
                .referenceId(applicationId)
                .referenceType("TRAINING_APPLICATION")
                .actionUrl("/training/application/" + applicationId)
                .actorId(0L) // 시스템 자동 처리
                .build();
    }
}

