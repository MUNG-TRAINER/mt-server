package com.mungtrainer.mtserver.user.service;

import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import com.mungtrainer.mtserver.user.dto.request.UserProfileUpdateRequest;
import com.mungtrainer.mtserver.user.dto.response.UserProfileResponse;
import com.mungtrainer.mtserver.user.entity.User;
import com.mungtrainer.mtserver.user.mapper.UserMapper;
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
     * 공통 프로필 조회
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .name(user.getName())
                .birth(user.getBirth())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .isPublic(user.getIsPublic())
                .role(user.getRole())
                .sido(user.getSido())
                .sigungu(user.getSigungu())
                .roadname(user.getRoadname())
                .restAddress(user.getRestAddress())
                .postcode(user.getPostcode())
                .build();
    }

    // 훈련사 프로필 조회
    public TrainerResponse getTrainerProfileById(Long trainerId) {

        TrainerProfile profile = userMapper.findByTrainerId(trainerId);

        if (profile == null) {
            throw new RuntimeException("훈련사 프로필을 찾을 수 없습니다.");
        }
        // DB에 저장된 파일 key
        String fileKey = profile.getCertificationImageUrl();

        // presigned URL 생성
        String presignedUrl = s3Service.generateDownloadPresignedUrl(fileKey);


        return TrainerResponse.builder()
                .trainerId(profile.getTrainerId())
                .careerInfo(profile.getCareerInfo())
                .introduce(profile.getIntroduce())
                .description(profile.getDescription())
                .style(profile.getStyle())
                .tag(profile.getTag())
                .certificationImageUrl(presignedUrl)
                .build();
    }

    /**
     * 공통 프로필 수정
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        // 사용자 존재 확인
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 업데이트할 정보 설정
        user.setName(request.getName());
        user.setBirth(request.getBirth());
        user.setPhone(request.getPhone());
        user.setProfileImage(request.getProfileImage());
        user.setIsPublic(request.getIsPublic());
        user.setSido(request.getSido());
        user.setSigungu(request.getSigungu());
        user.setRoadname(request.getRoadname());
        user.setRestAddress(request.getRestAddress());
        user.setPostcode(request.getPostcode());

        // Mybatis를 통한 업데이트
        int updated = userMapper.updateUserProfile(user);

        if (updated == 0) {
            throw new IllegalStateException("프로필 업데이트에 실패했습니다.");
        }

        // 업데이트된 정보 반환
        return getUserProfile(userId);
    }
}