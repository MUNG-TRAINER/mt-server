package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchResponse;
import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.service.TrainingCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class TrainingCourseController {
    private final TrainingCourseService courseService;

    /**
     * 훈련과정 검색 (무한 스크롤)
     * GET /api/course/search?keyword=기초&lastCourseId=123&size=20
     *
     * - USER: 자신이 속한 훈련사의 과정만 조회
     * - TRAINER: 자신이 등록한 과정만 조회
     *
     * @param keyword 검색 키워드
     * @param lastCourseId 마지막으로 조회한 courseId (다음 페이지 조회 시 사용)
     * @param size 조회할 항목 수
     * @param userDetails 인증된 사용자 정보
     */
    @GetMapping("/search")
    public ResponseEntity<CourseSearchResponse> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lastCourseId,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CourseSearchRequest request = CourseSearchRequest.builder()
                .keyword(keyword)
                .lastCourseId(lastCourseId)
                .size(size)
                .build();

        CourseSearchResponse response = courseService.searchCourses(request, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<TrainingCourseResponse> getCourse(@PathVariable Long courseId){
        TrainingCourseResponse courseResponse = courseService.getCourseById(courseId);
        return ResponseEntity.ok(courseResponse);
    }
}
