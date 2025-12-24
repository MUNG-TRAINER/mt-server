package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상담 신청용 반려견 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogForCounselingResponse {
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
    private String profileImage;

    /**
     * 이미 상담 신청했는지 여부
     */
    private Boolean alreadyRequested;

    /**
     * 상담 ID (신청한 경우)
     */
    private Long counselingId;
}

