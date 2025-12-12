package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCounselingResponse {
    private Long counselingId;
    private String message;
}
