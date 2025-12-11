package com.mungtrainer.mtserver.counseling.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingPostRequestDto {
    @NotBlank(message = "상담 내용은 필수입니다")
    private String content; // 상담 내용
}
