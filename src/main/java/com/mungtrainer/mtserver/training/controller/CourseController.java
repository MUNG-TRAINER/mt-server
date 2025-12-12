package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.request.CourseReuploadRequest;
import com.mungtrainer.mtserver.training.dto.request.CourseUpdateRequest;
import com.mungtrainer.mtserver.training.dto.request.CourseUploadRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseResponse;
import com.mungtrainer.mtserver.training.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer/course")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;

  @PostMapping
  public ResponseEntity<CourseResponse> uploadCourse(
      @Valid @RequestBody CourseUploadRequest courseUploadRequest,
      @AuthenticationPrincipal CustomUserDetails principal) {
    Long userId = principal.getUserId();
    return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(courseUploadRequest, userId));
  }

  @PostMapping("/{courseId}")
  public ResponseEntity<CourseResponse> reuploadCourse(
      @Valid @RequestBody CourseReuploadRequest request,
      @AuthenticationPrincipal CustomUserDetails principal) {
    Long userId = principal.getUserId();
    return ResponseEntity.status(HttpStatus.CREATED).body(courseService.courseReupload(request, userId));
  }

  @PatchMapping("/{courseId}")
  public ResponseEntity<CourseResponse> updateCourse(
      @Valid @RequestBody CourseUpdateRequest request,
      @PathVariable("courseId") Long courseId,
      @AuthenticationPrincipal CustomUserDetails principal) {
    Long userId = principal.getUserId();
    return ResponseEntity.ok(courseService.updateCourse(request, courseId, userId));
  }

  @DeleteMapping("/{courseId}")
  public ResponseEntity<Void> deleteCourse(
      @PathVariable("courseId") Long courseId,
      @AuthenticationPrincipal CustomUserDetails principal) {
    Long userId = principal.getUserId();
    courseService.deleteCourse(courseId, userId);
    // 204 응답
    return ResponseEntity.noContent().build();
  }
}
