package com.mungtrainer.mtserver.user.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 사용자 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    /**
     * 사용자 ID (PK)
     */
    private Long userId;

    /**
     * 사용자명 (UNIQUE)
     */
    private String userName;

    /**
     * 이름
     */
    private String name;

    /**
     * 생년월일
     */
    private LocalDate birth;

    /**
     * 이메일
     */
    private String email;

    /**
     * 전화번호
     */
    private String phone;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 프로필 이미지
     */
    private String profileImage;

    /**
     * 공개 여부
     */
    private Boolean isPublic;

    /**
     * 역할 (USER, TRAINER, ADMIN)
     */
    private String role;

    /**
     * 약관 동의 여부
     */
    private Boolean isAgree;

    /**
     * 시/도
     */
    private String sido;

    /**
     * 시/군/구
     */
    private String sigungu;

    /**
     * 도로명
     */
    private String roadname;

    /**
     * 상세 주소
     */
    private String restAddress;

    /**
     * 우편번호
     */
    private String postcode;

    /**
     * 차단 여부
     */
    private Boolean isBlocked;

    /**
     * 리프레시 토큰
     */
    private String refreshToken;
}

