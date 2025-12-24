package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MultiCourseCategoryResponse {
    private String tags;
    private List<MultiCourseGroupResponse> courses;
}

