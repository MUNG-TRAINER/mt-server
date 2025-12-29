package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.training.dto.request.AttendanceUpdateRequest;
import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
import com.mungtrainer.mtserver.training.service.TrainingAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<AttendanceListResponse> attendanceList = trainingAttendanceService.getAttendanceList(sessionId);

        return ResponseEntity.ok(attendanceList);
    }

    /**
     * 특정 반려견의 출석 상태 변경
     *
     * PATCH /api/trainer/course/{courseId}/session/{sessionId}/attendance/{attendanceId}
     */
    @PatchMapping("/{attendanceId}")
    public ResponseEntity<Void> updateAttendanceStatus(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            @PathVariable Long attendanceId,
            @Valid @RequestBody AttendanceUpdateRequest request
    ) {
        trainingAttendanceService.updateAttendanceStatus(attendanceId, request);

        return ResponseEntity.ok().build();
    }
}