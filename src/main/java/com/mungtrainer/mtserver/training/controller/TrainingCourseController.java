package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.dto.response.CalendarResponse;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchResponse;
import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.service.TrainingCourseApplicationService;
import com.mungtrainer.mtserver.training.service.TrainingCourseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class TrainingCourseController {
    private final TrainingCourseService courseService;
    private final TrainingCourseApplicationService applicationService;

    /**
     * 훈련과정 검색 (무한 스크롤)
     * GET /api/course/search?keyword=기초&lastCourseId=123&size=20&lessonForm=WALK
     *
     * - USER: 자신이 속한 훈련사의 과정만 조회
     * - TRAINER: 자신이 등록한 과정만 조회
     *
     * @param keyword 검색 키워드
     * @param lastCourseId 마지막으로 조회한 courseId (다음 페이지 조회 시 사용)
     * @param size 조회할 항목 수
     * @param lessonForm 훈련 형태 필터 (WALK, GROUP, PRIVATE)
     * @param userDetails 인증된 사용자 정보
     */
    @GetMapping("/search")
    public ResponseEntity<CourseSearchResponse> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastCourseId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) String lessonForm,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // lessonForm validation
        if (lessonForm != null && !isValidLessonForm(lessonForm)) {
            throw new CustomException(ErrorCode.INVALID_LESSON_FORM);
        }

        CourseSearchRequest request = CourseSearchRequest.builder()
                .keyword(keyword)
                .lastCourseId(lastCourseId)
                .size(size)
                .lessonForm(lessonForm)
                .build();

        CourseSearchResponse response = courseService.searchCourses(request, userDetails);
        return ResponseEntity.ok(response);
    }

    /**
     * lessonForm 값 검증 메서드
     */
    private boolean isValidLessonForm(String lessonForm) {
        return "WALK".equals(lessonForm)
                || "GROUP".equals(lessonForm)
                || "PRIVATE".equals(lessonForm);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<TrainingCourseResponse> getCourse(@PathVariable Long courseId){
        TrainingCourseResponse courseResponse = courseService.getCourseById(courseId);
        return ResponseEntity.ok(courseResponse);
    }

    @PostMapping("/{courseId}/apply")
    public ResponseEntity<List<ApplicationResponse>> applyCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid ApplicationRequest request
    ) {
        List<ApplicationResponse> created = applicationService.applyCourse(
                principal.getUserId(), courseId, request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 달력용 세션 날짜 조회
     * GET /api/course/calendar?startDate=2024-01-01&endDate=2024-01-31&keyword=기초&lessonForm=WALK
     *
     * - USER: 자신이 속한 훈련사의 세션만 조회
     * - TRAINER: 자신이 등록한 세션만 조회
     *
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @param keyword 검색 키워드 (선택)
     * @param lessonForm 수업 형태 필터 (WALK, GROUP, PRIVATE) (선택)
     * @param userDetails 인증된 사용자 정보
     * @return 세션이 있는 날짜 목록
     */
    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lessonForm,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 날짜 유효성 검증
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        // lessonForm 유효성 검증
        if (lessonForm != null && !isValidLessonForm(lessonForm)) {
            throw new CustomException(ErrorCode.INVALID_LESSON_FORM);
        }

        CalendarResponse response = courseService.getCalendarByPeriod(
                startDate, endDate, userDetails, keyword, lessonForm);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜의 코스 목록 조회 (CourseSearchResponse 형식)
     * GET /api/course/calendar/courses?date=2024-01-15&keyword=기초&lessonForm=WALK
     *
     * - USER: 자신이 속한 훈련사의 코스만 조회
     * - TRAINER: 자신이 등록한 코스만 조회
     *
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @param keyword 검색 키워드 (선택)
     * @param lessonForm 수업 형태 필터 (WALK, GROUP, PRIVATE) (선택)
     * @param userDetails 인증된 사용자 정보
     * @return 해당 날짜의 코스 목록 (CourseSearchResponse 형식)
     */
    @GetMapping("/calendar/courses")
    public ResponseEntity<CourseSearchResponse> getCoursesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lessonForm,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // lessonForm 유효성 검증
        if (lessonForm != null && !isValidLessonForm(lessonForm)) {
            throw new CustomException(ErrorCode.INVALID_LESSON_FORM);
        }

        CourseSearchResponse response = courseService.getCoursesByDate(
                date, userDetails, keyword, lessonForm);

        return ResponseEntity.ok(response);
    }
}
