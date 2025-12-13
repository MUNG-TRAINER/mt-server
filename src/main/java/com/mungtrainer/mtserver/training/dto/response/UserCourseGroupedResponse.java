package com.mungtrainer.mtserver.training.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserCourseGroupedResponse {
    private Long courseId;
    private String title;
    private String mainImage;
    private String lessonForm;
    private String difficulty;
    private String location;
    private String type;

    private List<UserCourseSessionResponse> sessions = new ArrayList<>();
}
