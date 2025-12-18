package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.request.UpdateSessionRequest;
import com.mungtrainer.mtserver.training.dto.response.TrainingSessionResponse;
import com.mungtrainer.mtserver.training.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course/{courseId}/sessions")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    /**
     * 특정 과정의 세션 목록 조회
     * GET /api/course/{courseId}/sessions
     */
    @GetMapping
    public ResponseEntity<List<TrainingSessionResponse>> getSessionList(
            @PathVariable Long courseId
    ) {
        List<TrainingSessionResponse> sessions = trainingSessionService.getSessionsByCourseId(courseId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 특정 세션 상세 조회
     * GET /api/course/{courseId}/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<TrainingSessionResponse> getSessionDetail(
            @PathVariable Long courseId,
            @PathVariable Long sessionId
    ) {
        TrainingSessionResponse session = trainingSessionService.getSessionById(courseId, sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * 세션 수정
     * PATCH /api/course/{courseId}/sessions/{sessionId}
     */
    @PatchMapping("/{sessionId}")
    public ResponseEntity<Void> updateSession(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        trainingSessionService.updateSession(sessionId, customUserDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 세션 삭제
     * DELETE /api/course/{courseId}/sessions/{sessionId}
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        trainingSessionService.deleteSession(sessionId, customUserDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}