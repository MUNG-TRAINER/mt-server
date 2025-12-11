package com.mungtrainer.mtserver.user.service;

import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.user.dto.request.UserImageUploadRequest;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserImageUploadResponse;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.entity.User;
import com.mungtrainer.mtserver.user.dao.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserMapper userMapper;
    private final S3Service s3Service;

    /**
     * 사용자 프로필 조회
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return convertToResponse(user);
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

        // 새 이미지가 있고, 기존 이미지와 다른 경우에만 삭제
        if (newImageKey != null
                && !newImageKey.isBlank()
                && oldImageKey != null
                && !oldImageKey.isBlank()
                && !newImageKey.equals(oldImageKey)) {
            s3Service.deleteFile(oldImageKey);
        }

      // null이 아닌 필드만 업데이트
      if (request.getName() != null) {
          user.setName(request.getName());
      }
      if (request.getBirth() != null) {
          user.setBirth(request.getBirth());
      }
      if (request.getPhone() != null) {
          user.setPhone(request.getPhone());
      }
      if (request.getProfileImage() != null) {
          user.setProfileImage(request.getProfileImage());
      }
      if (request.getIsPublic() != null) {
          user.setIsPublic(request.getIsPublic());
      }
      if (request.getSido() != null) {
          user.setSido(request.getSido());
      }
      if (request.getSigungu() != null) {
          user.setSigungu(request.getSigungu());
      }
      if (request.getRoadname() != null) {
          user.setRoadname(request.getRoadname());
      }
      if (request.getRestAddress() != null) {
          user.setRestAddress(request.getRestAddress());
      }
      if (request.getPostcode() != null) {
          user.setPostcode(request.getPostcode());
      }

        int result = userMapper.updateUserProfile(user);
        if (result == 0) {
            throw new CustomException(ErrorCode.PROFILE_UPDATE_FAILED);
        }
        return convertToResponse(user);
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
     * User 엔티티를 UserProfileResponse로 변환
     * S3에 저장된 이미지 키를 Presigned URL로 변환
     */
    private UserProfileResponse convertToResponse(User user) {
        String profileImageUrl = null;
        if (user.getProfileImage() != null && !user.getProfileImage().isBlank()) {
            profileImageUrl = s3Service.generateDownloadPresignedUrl(user.getProfileImage());
        }

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
                .build();
    }
}