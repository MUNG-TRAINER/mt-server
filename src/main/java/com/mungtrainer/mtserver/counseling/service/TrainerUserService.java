package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDao;
import com.mungtrainer.mtserver.counseling.dto.response.TrainerUserListResponseDto;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.mapper.DogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerUserService {

    private final DogMapper dogMapper;
    private final TrainerUserDao trainerUserDao;
    private final S3Service s3Service;

    public List<TrainerUserListResponseDto> getUsersByTrainer(Long trainerId) {
        return trainerUserDao.findUsersByTrainerId(trainerId);
    }

    // 반려견 목록 조회
    public List<DogResponse> getDogsByUser(Long userId) {

        // 1. DB에서 반려견 리스트 조회
        List<DogResponse> dogs = dogMapper.selectDogsByUserId(userId);

        // 2. S3 Presigned URL 발급
        return dogs.stream()
                .map(dog -> {
                    // DB에 저장된 S3 key를 가져와서 Presigned URL 생성
                    String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
                    // 최종적으로 DTO 필드에 URL 세팅
                    dog.setProfileImage(presignedUrl);
                    return dog;
                })
                .collect(Collectors.toList());
    }
}
