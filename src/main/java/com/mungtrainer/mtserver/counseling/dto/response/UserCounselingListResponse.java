package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 상담 목록 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCounselingListResponse {
    /**
     * 상담 ID
     */
    private Long counselingId;

    /**
     * 반려견 ID
     */
    private Long dogId;

    /**
     * 반려견 이름
     */
    private String dogName;

    /**
     * 반려견 프로필 이미지
     */
    private String dogImage;

    /**
     * 연락처
     */
    private String phone;

    /**
     * 상담 완료 여부
     */
    private Boolean isCompleted;

    /**
     * 상담 신청일
     */
    private LocalDateTime createdAt;

    /**
     * 상담 내용 (완료된 경우에만)
     */
    private String content;
}

