package com.mungtrainer.mtserver.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mungtrainer.mtserver.auth.service.AuthService.Role;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.user.dao.UserDAO;
import com.mungtrainer.mtserver.user.dto.request.UserImageUploadRequest;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserFCMTokenResponse;
import com.mungtrainer.mtserver.user.dto.response.UserImageUploadResponse;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserDAO userMapper;
	private final S3Service s3Service;

	/**
	 * 내 프로필 조회 (전체 정보)
	 */
	public UserProfileResponse getUserProfile(Long userId) {
		User user = userMapper.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		String profileImageUrl = getProfileImageUrl(user);
		return UserProfileResponse.createFullProfile(user, profileImageUrl);
	}

	/**
	 * 사용자명으로 프로필 조회 (권한에 따라 다른 정보 반환)
	 * @param userName 조회할 사용자명
	 * @param currentUserId 현재 로그인한 사용자 ID
	 * @param currentUserRole 현재 로그인한 사용자 역할
	 */
	public UserProfileResponse getUserProfileByUserName(String userName, Long currentUserId, String currentUserRole) {
		User targetUser = userMapper.findByUserName(userName)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		String profileImageUrl = getProfileImageUrl(targetUser);

		// 1. 본인 조회
		if (targetUser.getUserId().equals(currentUserId)) {
			return UserProfileResponse.createFullProfile(targetUser, profileImageUrl);
		}

		// 2. 트레이너가 조회
		if (Role.TRAINER.name().equals(currentUserRole)) {
			boolean isConnected = userMapper.isConnectedToTrainer(targetUser.getUserId(), currentUserId);
			if (isConnected) {
				return UserProfileResponse.createTrainerAccessProfile(targetUser, profileImageUrl);
			}
		}

		// 3. 타인이 조회 - 공개 여부 확인
		if (!Boolean.TRUE.equals(targetUser.getIsPublic())) {
			throw new CustomException(ErrorCode.PROFILE_NOT_PUBLIC);
		}

		return UserProfileResponse.createPublicProfile(targetUser, profileImageUrl);
	}

	/**
	 * 사용자 프로필 수정
	 */
	@Transactional
	public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
		User user = userMapper.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 프로필 이미지 변경 감지 및 기존 S3 파일 삭제
		String oldImageKey = user.getProfileImage();
		String newImageKey = request.getProfileImage();

		// profileImage 필드가 요청에 포함된 경우에만 처리
		if (newImageKey != null && oldImageKey != null && !oldImageKey.isBlank()) {
			// Case 1: 빈 문자열("")로 이미지 삭제 요청
			if (newImageKey.isBlank()) {
				s3Service.deleteFile(oldImageKey);
			}
			// Case 2: 새 이미지로 변경 (기존 이미지와 다른 경우)
			else if (!newImageKey.equals(oldImageKey)) {
				s3Service.deleteFile(oldImageKey);
			}
			// Case 3: 같은 이미지면 아무것도 안 함
		}
		// profileImage 필드가 없으면 (null) 이미지 유지

		updateUserFields(user, request);

		int result = userMapper.updateUserProfile(user);
		if (result == 0) {
			throw new CustomException(ErrorCode.PROFILE_UPDATE_FAILED);
		}

		String profileImageUrl = getProfileImageUrl(user);
		return UserProfileResponse.createFullProfile(user, profileImageUrl);
	}

	/**
	 * 프로필 이미지 업로드용 Presigned URL 발급
	 * @param userId 사용자 ID
	 * @param request 파일 키 및 콘텐츠 타입
	 * @return 업로드 URL
	 */
	public UserImageUploadResponse generateUploadUrl(Long userId, UserImageUploadRequest request) {
		// 사용자 존재 여부 확인
		userMapper.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		String uploadUrl = s3Service.generateUploadPresignedUrl(
			request.getFileKey(),
			request.getContentType());

		return UserImageUploadResponse.builder()
			.uploadUrl(uploadUrl)
			.build();
	}

	/**
	 * 사용자 반려견 프로필 공개 여부 변경
	 * @param userId 사용자 ID
	 * @param isPublic 공개 여부
	 */
	@Transactional
	public void updatePublicStatus(Long userId, Boolean isPublic) {
		int result = userMapper.updatePublicStatus(userId, isPublic);
		if (result == 0) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}
	}

	public UserFCMTokenResponse findUserFCMToken(Long userId) {
		Optional<String> fcmToken = userMapper.findUserFCMToken(userId);
		return UserFCMTokenResponse.builder().fcmToken(fcmToken.orElse(null)).build();
	}

	/**
	 * 사용자 필드 업데이트 (Helper 메서드)
	 */
	private void updateUserFields(User user, UserProfileUpdateRequest request) {
		if (request.getName() != null)
			user.setName(request.getName());
		if (request.getBirth() != null)
			user.setBirth(request.getBirth());
		if (request.getPhone() != null)
			user.setPhone(request.getPhone());
		if (request.getProfileImage() != null)
			user.setProfileImage(request.getProfileImage());
		if (request.getIsPublic() != null)
			user.setIsPublic(request.getIsPublic());
		if (request.getSido() != null)
			user.setSido(request.getSido());
		if (request.getSigungu() != null)
			user.setSigungu(request.getSigungu());
		if (request.getRoadname() != null)
			user.setRoadname(request.getRoadname());
		if (request.getRestAddress() != null)
			user.setRestAddress(request.getRestAddress());
		if (request.getPostcode() != null)
			user.setPostcode(request.getPostcode());
	}

	/**
	 * 프로필 이미지 URL 생성 (Helper 메서드)
	 */
	private String getProfileImageUrl(User user) {
		if (user.getProfileImage() != null && !user.getProfileImage().isBlank()) {
			return s3Service.generateDownloadPresignedUrl(user.getProfileImage());
		}
		return null;
	}

}