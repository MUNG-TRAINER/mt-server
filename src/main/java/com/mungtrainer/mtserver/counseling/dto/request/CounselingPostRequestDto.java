package com.mungtrainer.mtserver.counseling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingPostRequestDto {
    private String content; // 상담 내용
}
