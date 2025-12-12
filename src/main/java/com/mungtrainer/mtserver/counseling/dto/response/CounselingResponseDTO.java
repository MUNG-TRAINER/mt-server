package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 상담 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselingResponseDTO {

    /**
     * 상담 ID
     */
    private Long counselingId;

    /**
     * 상담 대상 반려견 ID
     */
    private Long dogId;

    /**
     * 상담 내용
     */
    private String content;

    /**
     * 상담 작성자 (훈련사 ID)
     */
    private Long trainerId;

    /**
     * 상담 완료 여부
     */
    private Boolean isCompleted;

    /**
     * 상담 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 상담 수정일시
     */
    private LocalDateTime updatedAt;
}
