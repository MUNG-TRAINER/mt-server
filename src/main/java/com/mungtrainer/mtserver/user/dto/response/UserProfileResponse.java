package com.mungtrainer.mtserver.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 공통 프로필 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String userName;
    private String name;
    private LocalDate birth;
    private String email;
    private String phone;
    private String profileImage;
    private Boolean isPublic;
    private String role;

    // 주소 정보
    private String sido;
    private String sigungu;
    private String roadname;
    private String restAddress;
    private String postcode;
}