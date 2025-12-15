package com.mungtrainer.mtserver.trainer.service;

import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.trainer.dao.TrainerDAO;
import com.mungtrainer.mtserver.trainer.dto.request.TrainerProfileUpdateRequest;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerDAO trainerDao;
    private final S3Service s3Service;

    // 훈련사 프로필 조회
    public TrainerResponse getTrainerProfileById(Long trainerId) {

        TrainerProfile profile = trainerDao.findById(trainerId);

        if (profile == null) {
            throw new RuntimeException("훈련사 프로필을 찾을 수 없습니다.");
        }
        // DB에 저장된 파일 key
        String fileKey = profile.getCertificationImageUrl();

        // presigned URL 생성
        String presignedUrl = null;
        //null/빈 문자열 체크 후 처리
        if(fileKey != null && !fileKey.isBlank()) {
            presignedUrl = s3Service.generateDownloadPresignedUrl(fileKey);
        }

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

     // 로그인한 훈련사 프로필 수정
    public TrainerResponse updateTrainerProfile(TrainerProfileUpdateRequest request,  Long userId) {

        // DB에 존재하는지 체크
        TrainerProfile profile = trainerDao.findById(userId);
        if (profile == null) {
            throw new RuntimeException("훈련사 프로필이 존재하지 않습니다.");
        }

        // 수정
        if(request.getCareerInfo() != null) profile.setCareerInfo(request.getCareerInfo());
        if(request.getIntroduce() != null) profile.setIntroduce(request.getIntroduce());
        if(request.getDescription() != null) profile.setDescription(request.getDescription());
        if(request.getStyle() != null) profile.setStyle(request.getStyle());
        if(request.getTag() != null) profile.setTag(request.getTag());
        if(request.getCertificationImageUrl() != null) profile.setCertificationImageUrl(request.getCertificationImageUrl());

        trainerDao.updateTrainerProfile(profile);

        return TrainerResponse.builder()
                .trainerId(profile.getTrainerId())
                .careerInfo(profile.getCareerInfo())
                .introduce(profile.getIntroduce())
                .description(profile.getDescription())
                .style(profile.getStyle())
                .tag(profile.getTag())
                .certificationImageUrl(profile.getCertificationImageUrl())
                .build();
    }

    // S3 Presigned URL 생성
    public String generateCertificationUploadUrl(Long userId, String contentType) {
        if(contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("허용되지 않은 파일 형식입니다.");
        }
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileKey = "trainer-certification/" + userId + "/cert-" + System.currentTimeMillis() + extension;
        return s3Service.generateUploadPresignedUrl(fileKey, contentType);
    }
}
