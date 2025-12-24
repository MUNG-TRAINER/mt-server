package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 상담 상세 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCounselingDetailResponse {
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
     * 반려견 품종
     */
    private String breed;

    /**
     * 반려견 나이
     */
    private Integer age;

    /**
     * 반려견 성별
     */
    private String gender;

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

    /**
     * 상담 완료일 (완료된 경우에만)
     */
    private LocalDateTime updatedAt;
}

