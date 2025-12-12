package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MultiCourseCategoryDTO {
    private String tags;
    private List<MultiCourseGroupDTO> courses;
}

