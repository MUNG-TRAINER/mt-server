package com.mungtrainer.mtserver.dog.service;

import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.dog.dao.DogDAO;
import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogImageUploadRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
import com.mungtrainer.mtserver.dog.dto.response.DogImageUploadResponse;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.order.dao.WishlistDAO;
import com.mungtrainer.mtserver.training.dao.TrainingCourseApplicationDAO;
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

    private final DogDAO dogDAO;
    private final S3Service s3Service;
    private final TrainingCourseApplicationDAO trainingCourseApplicationDAO;
    private final WishlistDAO wishlistDAO;

    /**
     * 반려견 정보 생성
     * @param userId 사용자 ID
     * @param request 반려견 생성 요청 정보 (profileImageUrl 포함)
     * @return 생성된 반려견 ID
     */
    @Transactional
    public Long createDog(Long userId, DogCreateRequest request) {

        // 이름 중복 확인
        if (dogDAO.existsByUserIdAndName(userId, request.getName())) {
            throw new CustomException(ErrorCode.DOG_NAME_DUPLICATE);
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
          int result = dogDAO.insertDog(params);
        } catch (DuplicateKeyException e) {
           throw new CustomException(ErrorCode.DOG_NAME_DUPLICATE);
        }

        // 생성된 ID 가져오기
        Long dogId = (Long) params.get("dogId");
        if (dogId == null) {
            throw new CustomException(ErrorCode.DOG_ID_GENERATION_FAILED);
        }
        return dogId;
    }

    /**
     * 반려견 정보 조회
     * @param dogId 반려견 ID
     * @return 반려견 정보
     */
    public DogResponse getDog(Long dogId) {
        DogResponse dog = dogDAO.selectDogById(dogId);
        if (dog == null) {
          throw new CustomException(ErrorCode.DOG_NOT_FOUND);
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
        List<DogResponse> dogs = dogDAO.selectDogsByUserId(userId);

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
        List<DogResponse> dogs = dogDAO.selectDogsByUsername(username);

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
        // 소유자 확인
        DogResponse dog = dogDAO.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
            throw new CustomException(ErrorCode.DOG_NO_PERMISSION);
        }

        // 이름 변경 시 중복 확인
        if (request.getName() != null
            && !request.getName().isBlank()
            && !request.getName().equals(dog.getName())) {
          if (dogDAO.existsByUserIdAndName(userId, request.getName())) {
              throw new CustomException(ErrorCode.DOG_NAME_DUPLICATE);
          }
        }

        // 프로필 이미지 변경 감지 및 기존 S3 파일 삭제
        String oldImageKey = dog.getProfileImage();
        String newImageKey = request.getProfileImageUrl();

        // 새 이미지가 있고, 기존 이미지와 다른 경우에만 삭제
        if (newImageKey != null
            && !newImageKey.isBlank()
            && oldImageKey != null
            && !oldImageKey.isBlank()
            && !newImageKey.equals(oldImageKey)) {
          s3Service.deleteFile(oldImageKey);
        }

        // 반려견 정보 수정 (프로필 이미지 URL 포함)
        int result = dogDAO.updateDog(dogId, userId, request, userId);
        if (result == 0) {
            throw new CustomException(ErrorCode.DOG_UPDATE_FAILED);
        }
    }

    /**
     * 반려견 정보 삭제
     * @param userId 사용자 ID
     * @param dogId 반려견 ID
     */
    @Transactional
    public void deleteDog(Long userId, Long dogId) {
      // 1. 소유자 확인
      DogResponse dog = dogDAO.selectDogByIdAndUserId(dogId, userId);
      if (dog == null) {
        throw new CustomException(ErrorCode.DOG_NO_PERMISSION);
      }

      // 2. 진행 중인 훈련 과정 확인
      boolean hasActiveTraining = trainingCourseApplicationDAO.existsActiveByDogId(dogId);
      if (hasActiveTraining) {
        throw new CustomException(ErrorCode.DOG_HAS_ACTIVE_TRAINING);
      }

      // 3. 연관 데이터 처리 (위시리스트 소프트 삭제)
      wishlistDAO.softDeleteByDogId(dogId, userId);

      // 4. S3 파일 먼저 삭제 (실패 시 DB 작업도 롤백됨)
      if (dog.getProfileImage() != null && !dog.getProfileImage().isBlank()) {
        s3Service.deleteFile(dog.getProfileImage());
        log.info("S3 이미지 삭제 완료. dogId: {}, imageKey: {}", dogId, dog.getProfileImage());
      }

      // 5. 반려견 소프트 삭제
      int result = dogDAO.deleteDog(dogId, userId, userId);
      if (result == 0) {
        throw new CustomException(ErrorCode.DOG_DELETE_FAILED);
      }

      log.info("반려견 소프트 삭제 완료. dogId: {}, S3 이미지 즉시 삭제됨", dogId);
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
      if (dogId != null) {
        DogResponse dog = dogDAO.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
            throw new CustomException(ErrorCode.DOG_NO_PERMISSION);
        }
      }

      String uploadUrl = s3Service.generateUploadPresignedUrl(request.getFileKey(), request.getContentType());

      return DogImageUploadResponse.builder()
          .uploadUrl(uploadUrl)
          .build();
    }
}