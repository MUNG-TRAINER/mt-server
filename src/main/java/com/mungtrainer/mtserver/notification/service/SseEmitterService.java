package com.mungtrainer.mtserver.notification.service;

import com.mungtrainer.mtserver.notification.dao.NotificationDAO;
import com.mungtrainer.mtserver.notification.dao.NotificationSseClientDAO;
import com.mungtrainer.mtserver.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterService {

    private final NotificationSseClientDAO sseClientDao;
    private final NotificationDAO notificationDao;

    // userId -> emitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final Long DEFAULT_TIMEOUT = 30L * 60 * 1000;

    /**
     * SSE 연결 생성
     */
    public SseEmitter connect(Long userId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.put(userId, emitter);
        sseClientDao.upsertActiveClient(userId, true, userId);

        log.info("SSE 연결 생성 userId={}, lastEventId={}", userId, lastEventId);


        emitter.onCompletion(() -> disconnect(userId));
        emitter.onTimeout(() -> handleTimeout(userId, emitter));
        emitter.onError(e -> handleError(userId, emitter, e));

        // 1. 최초 연결 확인 이벤트
        sendConnectEvent(emitter);

        // 2. 재연결 시 놓친 알림 재전송
        if (lastEventId != null) {
            resendMissedNotifications(userId, lastEventId, emitter);
        }

        return emitter;
    }

    private void resendMissedNotifications(
            Long userId,
            String lastEventId,
            SseEmitter emitter
    ) {
        try {
            Long lastId = Long.valueOf(lastEventId);

            List<Notification> missedNotifications =
                    notificationDao.findAfterId(userId, lastId);

            for (Notification notification : missedNotifications) {
                emitter.send(
                        SseEmitter.event()
                                .id(notification.getNotificationId().toString())
                                .name("notification")
                                .data(notification)
                );
            }

            log.info("놓친 알림 재전송 완료 userId={}, count={}",
                    userId, missedNotifications.size());

        } catch (Exception e) {
            log.error("놓친 알림 재전송 실패 userId={}", userId, e);
        }
    }

    /**
     * 알림 전송
     */
    public boolean send(Long targetUserId, Notification notification) {
        SseEmitter emitter = emitters.get(targetUserId);

        if (emitter == null) {
            log.warn("SSE 미연결 상태 userId={}", targetUserId);
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .id(notification.getNotificationId().toString())
                    .name("notification")
                    .data(notification));

            sseClientDao.updateLastEventId(
                    targetUserId,
                    notification.getNotificationId()
            );

            log.info("SSE 알림 전송 성공 userId={}, notificationId={}",
                    targetUserId, notification.getNotificationId());

            return true;

        } catch (IOException e) {
            log.error("SSE 전송 실패 userId={}", targetUserId, e);
            disconnect(targetUserId);
            return false;
        }
    }

    /**
     * 연결 종료 공통 처리
     */
    private void disconnect(Long userId) {
        emitters.remove(userId);
        sseClientDao.updateActive(userId, false);
        log.info("SSE 연결 종료 userId={}", userId);
    }

    /**
     * 타임아웃 처리
     */
    private void handleTimeout(Long userId, SseEmitter emitter) {
        sendReconnectEvent(emitter, "SSE 타임아웃 발생. 재연결 필요");
        disconnect(userId);
        emitter.complete();
    }

    /**
     * 에러 처리
     */
    private void handleError(Long userId, SseEmitter emitter, Throwable e) {
        log.error("SSE 에러 발생 userId={}", userId, e);
        sendReconnectEvent(emitter, "SSE 오류 발생. 재연결 필요");
        disconnect(userId);
        emitter.completeWithError(e);
    }

    /**
     * 최초 연결 확인 이벤트
     */
    private void sendConnectEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            log.warn("SSE connect 이벤트 전송 실패", e);
        }
    }

    /**
     * 재연결 안내 이벤트
     */
    private void sendReconnectEvent(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("reconnect")
                    .data(message)
                    .reconnectTime(3000L));
        } catch (IOException e) {
            log.warn("SSE reconnect 이벤트 전송 실패", e);
        }
    }
}

