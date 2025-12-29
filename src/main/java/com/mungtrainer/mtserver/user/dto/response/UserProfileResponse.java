package com.mungtrainer.mtserver.user.dto.response;

import java.time.LocalDate;

import com.mungtrainer.mtserver.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	private String fcmToken;

	/**
	 * 전체 프로필 (본인 조회)
	 */
	public static UserProfileResponse createFullProfile(User user, String profileImageUrl) {
		return UserProfileResponse.builder()
			.userId(user.getUserId())
			.userName(user.getUserName())
			.name(user.getName())
			.birth(user.getBirth())
			.email(user.getEmail())
			.phone(user.getPhone())
			.profileImage(profileImageUrl)
			.isPublic(user.getIsPublic())
			.role(user.getRole())
			.sido(user.getSido())
			.sigungu(user.getSigungu())
			.roadname(user.getRoadname())
			.restAddress(user.getRestAddress())
			.postcode(user.getPostcode())
			.fcmToken(user.getFcmToken())
			.build();
	}

	/**
	 * 공개 프로필 (타인이 조회 - 공개 설정된 경우)
	 */
	public static UserProfileResponse createPublicProfile(User user, String profileImageUrl) {
		return UserProfileResponse.builder()
			.userId(user.getUserId())
			.userName(user.getUserName())
			.name(user.getName())
			.email(user.getEmail())
			.profileImage(profileImageUrl)
			.fcmToken(user.getFcmToken())
			.build();
	}

	/**
	 * 트레이너 전용 프로필 (연결된 회원 조회)
	 */
	public static UserProfileResponse createTrainerAccessProfile(User user, String profileImageUrl) {
		return UserProfileResponse.builder()
			.userId(user.getUserId())
			.userName(user.getUserName())
			.name(user.getName())
			.email(user.getEmail())
			.phone(user.getPhone())
			.profileImage(profileImageUrl)
			.fcmToken(user.getFcmToken())
			.build();
	}
}