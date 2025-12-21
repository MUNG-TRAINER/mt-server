package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelCounselingResponse {
    private boolean success; // 취소 성공 여부
    private String message; // 결과 메시지
}
