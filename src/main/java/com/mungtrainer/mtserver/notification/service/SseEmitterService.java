package com.mungtrainer.mtserver.notification.service;

import com.mungtrainer.mtserver.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    // 사용자별 SSE 연결 저장소
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 기본 타임아웃 (30분)
    private static final Long DEFAULT_TIMEOUT = 30L * 60 * 1000;

    /**
     * SSE 연결 생성
     */
    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.put(userId, emitter);
        log.info("SSE 연결 생성 userId={}", userId);

        // 연결 종료 시 정리
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 완료 userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("SSE 타임아웃 userId={}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId);
            log.error("SSE 에러 userId={}", userId, e);
        });

        // 최초 연결 확인용 더미 이벤트 (안 보내면 일부 브라우저에서 바로 끊김)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패 userId={}", userId, e);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     */
    public void send(Long targetUserId, Notification notification) {
        SseEmitter emitter = emitters.get(targetUserId);

        if (emitter == null) {
            log.warn("❌ SSE 미연결 상태 userId={}", targetUserId);
            return;
        }

        try {
            log.info("📢 SSE 알림 전송 시도 userId={}, type={}",
                    targetUserId, notification.getType());

            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));

            log.info("✅ SSE 알림 전송 성공 userId={}", targetUserId);

        } catch (IOException e) {
            emitters.remove(targetUserId);
            log.error("🔥 SSE 전송 실패 userId={}", targetUserId, e);
        }
    }

}
