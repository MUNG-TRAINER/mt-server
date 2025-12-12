package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CounselingDogResponseDTO {
    private Long counselingId; // 상담 ID
    private String dogName;       // 반려견 이름
    private String ownerName;     // 보호자 이름
}
