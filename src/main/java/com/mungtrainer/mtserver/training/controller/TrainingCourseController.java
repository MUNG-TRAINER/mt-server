package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.service.TrainingCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class TrainingCourseController {
    private final TrainingCourseService courseService;

    @GetMapping("/{courseId}")
    public ResponseEntity<TrainingCourseResponse> getCourse(@PathVariable Long courseId){
        TrainingCourseResponse courseResponse = courseService.getCourseById(courseId);
        return ResponseEntity.ok(courseResponse);
    }
}
