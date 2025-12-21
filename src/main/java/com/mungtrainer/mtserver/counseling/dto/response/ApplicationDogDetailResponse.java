package com.mungtrainer.mtserver.counseling.dto.response;

import lombok.*;

/**
 * 신청 반려견 상세 정보 응답 DTO
 * 훈련사가 승인 대기 목록에서 상세 모달을 볼 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDogDetailResponse {

    /**
     * 반려견 ID
     */
    private Long dogId;

    /**
     * 반려견 이름
     */
    private String name;

    /**
     * 프로필 이미지 URL (Presigned URL)
     */
    private String profileImageUrl;

    /**
     * 나이
     */
    private Integer age;

    /**
     * 성별 (M/F)
     */
    private String gender;

    /**
     * 품종
     */
    private String breed;


    /**
     * 보호자 이름
     */
    private String ownerName;

    /**
     * 보호자 연락처
     */
    private String ownerPhone;
}

