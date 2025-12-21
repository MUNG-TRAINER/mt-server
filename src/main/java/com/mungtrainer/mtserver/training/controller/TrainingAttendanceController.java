package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.training.dto.request.AttendanceUpdateRequest;
import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
import com.mungtrainer.mtserver.training.service.TrainingAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trainer/course/{courseId}/session/{sessionId}/attendance")
public class TrainingAttendanceController {

    private final TrainingAttendanceService trainingAttendanceService;

    /**
     * 특정 세션의 출석 목록 조회
     *
     * GET /api/trainer/course/{courseId}/session/{sessionId}/attendance
     */
    @GetMapping
    public ResponseEntity<List<AttendanceListResponse>> getAttendanceList(
            @PathVariable Long courseId,
            @PathVariable Long sessionId
    ) {
        log.info("출석 목록 조회 요청 - courseId: {}, sessionId: {}", courseId, sessionId);

        List<AttendanceListResponse> attendanceList = trainingAttendanceService.getAttendanceList(sessionId);

        return ResponseEntity.ok(attendanceList);
    }

    /**
     * 특정 회원의 출석 상태 변경
     *
     * PATCH /api/trainer/course/{courseId}/session/{sessionId}/attendance/{userName}
     */
    @PatchMapping("/{userName}")
    public ResponseEntity<Void> updateAttendanceStatus(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            @PathVariable String userName,
            @Valid @RequestBody AttendanceUpdateRequest request
    ) {
        log.info("출석 상태 변경 요청 - courseId: {}, sessionId: {}, userName: {}, status: {}",
                courseId, sessionId, userName, request.getStatus());

        trainingAttendanceService.updateAttendanceStatus(sessionId, userName, request);

        return ResponseEntity.ok().build();
    }
}