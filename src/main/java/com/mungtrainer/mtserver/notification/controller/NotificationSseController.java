package com.mungtrainer.mtserver.notification.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.notification.entity.Notification;
import com.mungtrainer.mtserver.notification.service.NotificationService;
import com.mungtrainer.mtserver.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private final SseEmitterService sseEmitterService;
    private final NotificationService notificationService;


    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
//       lastEventId: 이미 notification 테이블에 있는 걸 SSE로 다시 “보내주는” 역할
        return sseEmitterService.connect(userId,lastEventId);
    }

    // 알림 조회 (모든 알림 또는 안 읽은 알림만)
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly
    ) {
        Long userId = userDetails.getUserId();
        List<Notification> notifications = notificationService.getNotifications(userId, unreadOnly);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.read(notificationId, userDetails.getUserId());
        return ResponseEntity.noContent().build(); // 204
    }

}