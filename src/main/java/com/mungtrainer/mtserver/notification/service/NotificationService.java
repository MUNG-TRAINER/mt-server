package com.mungtrainer.mtserver.notification.service;

import com.mungtrainer.mtserver.notification.dao.NotificationDAO;
import com.mungtrainer.mtserver.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationDAO notificationDao;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public void notifyCounselingRequest(
            Long trainerId,
            Long counselingId,
            Long createdBy
    ) {
        Notification notification = Notification.builder()
                .targetUserId(trainerId)
                .type("COUNSELING_REQUEST")
                .title("새 상담 신청")
                .message("반려견 상담 신청이 도착했습니다.")
                .referenceId(counselingId)
                .referenceType("COUNSELING")
                .actionUrl("/trainer/counseling/" + counselingId)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        notificationDao.insert(notification);

        // SSE 실시간 전송
        sseEmitterService.send(trainerId, notification);
    }
}

