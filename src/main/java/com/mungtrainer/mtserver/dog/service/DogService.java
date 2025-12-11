package com.mungtrainer.mtserver.dog.service;

import com.mungtrainer.mtserver.common.config.AwsS3Config;
import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogImageUploadRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
import com.mungtrainer.mtserver.dog.dto.response.DogImageUploadResponse;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.mapper.DogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 반려견 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DogService {

    private final DogMapper dogMapper;
    private final S3Service s3Service;
    private final AwsS3Config awsS3Config;

    /**
     * 반려견 정보 생성
     * @param userId 사용자 ID
     * @param request 반려견 생성 요청 정보 (profileImageUrl 포함)
     * @return 생성된 반려견 ID
     */
    @Transactional
    public Long createDog(Long userId, DogCreateRequest request) {
        log.info("반려견 생성 요청 - userId: {}, dogName: {}", userId, request.getName());

        // 이름 중복 확인
        if (dogMapper.existsByUserIdAndName(userId, request.getName())) {
          log.warn("반려견 이름 중복 - userId: {}, name: {}", userId, request.getName());
          throw new IllegalArgumentException("이미 등록된 반려견 이름입니다");
        }

        // 파라미터 맵 생성
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("request", request);
        params.put("createdBy", userId);
        params.put("updatedBy", userId);
        params.put("dogId", null); // MyBatis가 생성된 ID를 여기에 넣음

        // 반려견 정보 저장 (프로필 이미지 URL 포함)
        try {
          int result = dogMapper.insertDog(params);
        } catch (DuplicateKeyException e) {
          throw new IllegalArgumentException("이미 등록된 반려견 이름입니다");
        }

        // 생성된 ID 가져오기
        Long dogId = (Long) params.get("dogId");
        if (dogId == null) {
            throw new RuntimeException("생성된 반려견 ID를 가져오지 못했습니다");
        }

        log.info("반려견 생성 완료 - dogId: {}", dogId);
        return dogId;
    }

    /**
     * 반려견 정보 조회
     * @param dogId 반려견 ID
     * @return 반려견 정보
     */
    public DogResponse getDog(Long dogId) {
        log.info("반려견 조회 요청 - dogId: {}", dogId);

        DogResponse dog = dogMapper.selectDogById(dogId);
        if (dog == null) {
          log.warn("존재하지 않는 반려견 조회 시도 - dogId: {}", dogId);
          throw new IllegalArgumentException("존재하지 않는 반려견입니다");
        }

        // S3 키를 Presigned URL로 변환
        convertProfileImageToPresignedUrl(dog);

        return dog;
    }

    /**
     * 본인의 반려견 리스트 조회
     * @param userId 사용자 ID
     * @return 반려견 리스트
     */
    public List<DogResponse> getMyDogs(Long userId) {
        log.info("본인 반려견 리스트 조회 - userId: {}", userId);
        List<DogResponse> dogs = dogMapper.selectDogsByUserId(userId);

        // 각 반려견의 프로필 이미지를 Presigned URL로 변환
        convertProfileImagesToPresignedUrls(dogs);

        return dogs;
    }

    /**
     * 타인의 반려견 리스트 조회
     * @param username 사용자명
     * @return 반려견 리스트
     */
    public List<DogResponse> getUserDogs(String username) {
        log.info("타인 반려견 리스트 조회 - username: {}", username);
        List<DogResponse> dogs = dogMapper.selectDogsByUsername(username);

        // 각 반려견의 프로필 이미지를 Presigned URL로 변환
        convertProfileImagesToPresignedUrls(dogs);

        return dogs;
    }

    /**
     * 반려견 정보 수정
     * @param userId 사용자 ID
     * @param dogId 반려견 ID
     * @param request 수정 요청 정보 (profileImageUrl 포함)
     */
    @Transactional
    public void updateDog(Long userId, Long dogId, DogUpdateRequest request) {
      log.info("반려견 정보 수정 요청 - userId: {}, dogId: {}, request: name={}, age={}, weight={}, personality={}, profileImageUrl={}",
          userId, dogId, request.getName(), request.getAge(), request.getWeight(),
          request.getPersonality(), request.getProfileImageUrl());

        // 소유자 확인
        DogResponse dog = dogMapper.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
            throw new IllegalArgumentException("수정 권한이 없거나 존재하지 않는 반려견입니다");
        }

        // 이름 변경 시 중복 확인
        if (request.getName() != null
            && !request.getName().isBlank()
            && !request.getName().equals(dog.getName())) {
          if (dogMapper.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("이미 등록된 반려견 이름입니다: " + request.getName());
          }
        }

        // 프로필 이미지 변경 감지 및 기존 S3 파일 삭제
        String oldImageKey = dog.getProfileImage();
        String newImageKey = request.getProfileImageUrl();

        log.info("프로필 이미지 비교 - oldKey: {}, newKey: {}", oldImageKey, newImageKey);

        // 새 이미지가 있고, 기존 이미지와 다른 경우에만 삭제
        if (newImageKey != null
            && !newImageKey.isBlank()
            && oldImageKey != null
            && !oldImageKey.isBlank()
            && !newImageKey.equals(oldImageKey)) {

          log.info("기존 프로필 이미지 삭제 시작 - dogId: {}, oldKey: {}", dogId, oldImageKey);
          s3Service.deleteFile(oldImageKey);
          log.info("기존 프로필 이미지 삭제 완료");
        }

        // 반려견 정보 수정 (프로필 이미지 URL 포함)
        int result = dogMapper.updateDog(dogId, userId, request, userId);
        if (result == 0) {
            throw new RuntimeException("반려견 정보 수정에 실패했습니다");
        }

        log.info("반려견 정보 수정 완료 - dogId: {}, 수정된 행 수: {}", dogId, result);
    }

    /**
     * 반려견 정보 삭제
     * @param userId 사용자 ID
     * @param dogId 반려견 ID
     */
    @Transactional
    public void deleteDog(Long userId, Long dogId) {
        log.info("반려견 삭제 요청 - userId: {}, dogId: {}", userId, dogId);

        // 소유자 확인
        DogResponse dog = dogMapper.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
            throw new IllegalArgumentException("삭제 권한이 없거나 존재하지 않는 반려견입니다");
        }

        // S3 파일 먼저 삭제 (실패 시 DB 작업도 롤백됨)
        if (dog.getProfileImage() != null) {
          log.info("프로필 이미지 삭제 - dogId: {}, key: {}", dogId, dog.getProfileImage());
          s3Service.deleteFile(dog.getProfileImage()); // 실패 시 RuntimeException 발생 → 트랜잭션 롤백
        }

        // 소프트 삭제
        int result = dogMapper.deleteDog(dogId, userId, userId);
        if (result == 0) {
            throw new RuntimeException("반려견 정보 삭제에 실패했습니다");
        }

        log.info("반려견 삭제 완료 - dogId: {}", dogId);
    }

    /**
     * 단일 반려견의 프로필 이미지를 Presigned URL로 변환
     */
    private void convertProfileImageToPresignedUrl(DogResponse dog) {
      if (dog.getProfileImage() != null && !dog.getProfileImage().isBlank()) {
        String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
        dog.setProfileImage(presignedUrl);
      }
    }

    /**
     * 여러 반려견의 프로필 이미지를 Presigned URL로 변환
     */
    private void convertProfileImagesToPresignedUrls(List<DogResponse> dogs) {
      dogs.forEach(this::convertProfileImageToPresignedUrl);
    }

    /**
     * 프로필 이미지 업로드용 Presigned URL 발급
     * @param userId 사용자 ID (권한 확인용)
     * @param dogId 반려견 ID (수정 시에만 사용, 신규 등록 시 null)
     * @param request 파일 키 및 콘텐츠 타입
     * @return 업로드 URL 및 S3 키
     */
    public DogImageUploadResponse generateUploadUrl(Long userId, Long dogId, DogImageUploadRequest request) {
      log.info("프로필 이미지 업로드 URL 발급 요청 - userId: {}, dogId: {}, fileKey: {}, contentType: {}",
          userId, dogId, request.getFileKey(), request.getContentType());

      if (dogId != null) {
        DogResponse dog = dogMapper.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
          throw new IllegalArgumentException("수정 권한이 없거나 존재하지 않는 반려견입니다");
        }
      }

      String uploadUrl = s3Service.generateUploadPresignedUrl(request.getFileKey(), request.getContentType());

      log.info("업로드 URL 발급 완료 - fileKey: {}", request.getFileKey());

      return DogImageUploadResponse.builder()
          .uploadUrl(uploadUrl)
          .build();
    }
}