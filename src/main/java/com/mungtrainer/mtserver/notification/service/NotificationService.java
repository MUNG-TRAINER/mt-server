package com.mungtrainer.mtserver.notification.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.notification.dao.NotificationDAO;
import com.mungtrainer.mtserver.notification.dao.NotificationLogDAO;
import com.mungtrainer.mtserver.notification.entity.Notification;
import com.mungtrainer.mtserver.notification.entity.NotificationCommand;
import com.mungtrainer.mtserver.notification.entity.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationDAO notificationDao;
    private final NotificationLogDAO notificationLogDao;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public void send(NotificationCommand command) {

        // 1. Notification 생성
        Notification notification = Notification.builder()
                .targetUserId(command.getTargetUserId())
                .type(command.getType())
                .title(command.getTitle())
                .message(command.getMessage())
                .referenceId(command.getReferenceId())
                .referenceType(command.getReferenceType())
                .actionUrl(command.getActionUrl())
                .isRead(false)
                .createdBy(command.getActorId())
                .updatedBy(command.getActorId())
                .build();

        notificationDao.insert(notification);

        // 2. Log 생성 (PENDING)
        NotificationLog log = new NotificationLog();
        log.setNotificationId(notification.getNotificationId());
        log.setChannel("SSE");
        log.setStatus("PENDING");
        log.setCreatedBy(command.getActorId());
        log.setUpdatedBy(command.getActorId());

        notificationLogDao.insertLog(log);

        // 3. SSE 전송
        boolean success = sseEmitterService.send(
                command.getTargetUserId(),
                notification
        );

        // 4. Log 상태 업데이트
        notificationLogDao.updateStatus(
                log.getLogId(),
                success ? "SUCCESS" : "FAIL"
        );
    }

    @Transactional
    public void read(Long notificationId, Long userId) {

        // 1. 알림 존재 + 소유권 검증
        Notification notification = notificationDao.findById(notificationId);
        if (notification == null
                || !notification.getTargetUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        // 2. 읽음 처리 (멱등)
        int updated = notificationDao.markAsRead(notificationId, userId);

        if (updated == 0) {
            log.debug("알림 이미 읽음 또는 중복 요청 notificationId={}, userId={}",
                    notificationId, userId);
        }

        // updated == 0 인 경우:
        // - 이미 읽은 알림
        // - 중복 요청
        // → 정상 흐름
    }

}



