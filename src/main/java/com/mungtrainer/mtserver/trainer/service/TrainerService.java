package com.mungtrainer.mtserver.trainer.service;

import com.mungtrainer.mtserver.trainer.dao.TrainerDao;
import com.mungtrainer.mtserver.trainer.dto.request.TrainerProfileUpdateRequest;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.trainer.entity.TrainerProfile;
import com.mungtrainer.mtserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerDao trainerDao;

     // 훈련사 프로필 조회
    public TrainerResponse getTrainerProfileById(Long trainerId) {

        TrainerProfile profile = trainerDao.findById(trainerId);

        if (profile == null) {
            throw new RuntimeException("훈련사 프로필을 찾을 수 없습니다.");
        }

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

     // 로그인한 훈련사 프로필 수정
    public TrainerResponse updateTrainerProfile(TrainerProfileUpdateRequest request, User user) {

        Long trainerId = user.getUserId(); // 로그인한 사람의 ID

        // DB에 존재하는지 체크
        TrainerProfile profile = trainerDao.findById(trainerId);
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
}
