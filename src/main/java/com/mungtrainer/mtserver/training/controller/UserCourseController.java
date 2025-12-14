package com.mungtrainer.mtserver.training.controller;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.training.dto.response.UserCourseGroupedResponse;
import com.mungtrainer.mtserver.training.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/course")
@RequiredArgsConstructor
public class UserCourseController {

//    /users/course/?status=SCHEDULED
//    /users/course?status=DONE
    private final UserCourseService userCourseService;

    @GetMapping
    public List<UserCourseGroupedResponse> getUserCourses (
            @RequestParam String status,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Long userId = userDetails.getUserId();
        return userCourseService.getUserCourses(userId, status);
    }
}
