package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {

    @NotBlank(message = "출석 상태는 필수입니다")
    @Pattern(regexp = "ATTENDED|ABSENT|LATE|EXCUSED",
             message = "출석 상태는 ATTENDED, ABSENT, LATE, EXCUSED 중 하나여야 합니다")
    private String status;

    private String memo;
}