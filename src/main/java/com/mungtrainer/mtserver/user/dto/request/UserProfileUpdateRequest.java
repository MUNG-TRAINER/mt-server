package com.mungtrainer.mtserver.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 공통 프로필 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    private String name;
    private LocalDate birth;
    private String phone;
    private String profileImage;  // 프로필 이미지 URL
    private Boolean isPublic;

    // 주소 정보
    private String sido;
    private String sigungu;
    private String roadname;
    private String restAddress;
    private String postcode;
}