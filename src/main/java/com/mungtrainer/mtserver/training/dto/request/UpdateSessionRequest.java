package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionRequest {

    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String locationDetail;
    private String status;

    @Positive(message = "최대 인원은 양수여야 합니다")
    private Integer maxStudents;

    private String content;

    @Positive(message = "가격은 양수여야 합니다")
    private Integer price;
}