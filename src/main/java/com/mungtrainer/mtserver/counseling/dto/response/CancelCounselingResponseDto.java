package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CancelCounselingResponseDto {
    private boolean success; // 취소 성공 여부
    private String message; // 결과 메시지
}
