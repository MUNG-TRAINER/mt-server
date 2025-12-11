package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {

    @NotBlank(message = "출석 상태는 필수입니다")
    @Pattern(regexp = "PENDING|ATTENDED|ABSENT|LATE|EXCUSED",
             message = "출석 상태는 PENDING, ATTENDED, ABSENT, LATE, EXCUSED 중 하나여야 합니다")
    private String status;

    @Size(max = 500, message = "메모는 500자 이내로 입력해주세요")
    private String memo;
}