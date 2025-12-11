package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.request.CourseUploadRequest;
import com.mungtrainer.mtserver.training.dto.request.SessionUploadRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseUploadResponse;
import com.mungtrainer.mtserver.training.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer/course")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;

  @PostMapping
  public ResponseEntity<CourseUploadResponse> courseUpload(
      @Valid @RequestBody CourseUploadRequest courseUploadRequest,
      @AuthenticationPrincipal CustomUserDetails principal) {
    Long userId = principal.getUserId();
    return ResponseEntity.ok(courseService.createCourse(courseUploadRequest, userId));
  }

  @PostMapping("/{courseId}")
  public ResponseEntity<CourseUploadResponse> sessionUpload(
      @RequestBody SessionUploadRequest sessionUploadRequest,
      @PathVariable Long courseId,
      @AuthenticationPrincipal CustomUserDetails principal){
    Long userId = principal.getUserId();
    return ResponseEntity.ok(courseService.createSession(sessionUploadRequest,userId,courseId));
  }
}
