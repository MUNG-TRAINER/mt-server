package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDao;
import com.mungtrainer.mtserver.counseling.dto.response.TrainerUserListResponseDto;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.dao.DogMapper;
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
// 훈련사가 해당 회원을 관리하는지 확인
//        if (!isUserManagedByTrainer(trainerId, userId)) {
//            throw new UnauthorizedException("해당 회원의 정보에 접근할 권한이 없습니다.");
//        }
        // 1. DB에서 반려견 리스트 조회
        List<DogResponse> dogs = dogMapper.selectDogsByUserId(userId);

        if (dogs.isEmpty()) return List.of();

        // 2. 모든 반려견의 S3 키 추출
        List<String> imageKeys = dogs.stream()
                .map(DogResponse::getProfileImage)
                .collect(Collectors.toList());

        // 3. S3 Presigned URL 일괄 발급
        List<String> presignedUrls = s3Service.generateDownloadPresignedUrls(imageKeys);

        // 4. 각 반려견 객체에 URL 매핑
        for (int i = 0; i < dogs.size(); i++) {
            dogs.get(i).setProfileImage(presignedUrls.get(i));
        }

        return dogs;
    }
}
