package com.mungtrainer.mtserver.dog.service;

import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.mapper.DogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 반려견 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DogService {

    private final DogMapper dogMapper;
    // TODO: 이미지 업로드 서비스 추가 필요
    // private final ImageUploadService imageUploadService;

    /**
     * 반려견 정보 생성
     * @param userId 사용자 ID
     * @param request 반려견 생성 요청 정보
     * @param profileImage 프로필 이미지 (선택)
     * @return 생성된 반려견 ID
     */
    @Transactional
    public Long createDog(Long userId, DogCreateRequest request, MultipartFile profileImage) {
        log.info("반려견 생성 요청 - userId: {}, dogName: {}", userId, request.getName());

        // 이름 중복 확인
        if (dogMapper.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("이미 등록된 반려견 이름입니다: " + request.getName());
        }

        // 반려견 정보 저장
        int result = dogMapper.insertDog(userId, request, userId);
        if (result == 0) {
            throw new RuntimeException("반려견 정보 저장에 실패했습니다");
        }

        // 생성된 ID 조회
        Long dogId = dogMapper.selectLastInsertId();

        // 프로필 이미지 업로드 (선택사항)
        if (profileImage != null && !profileImage.isEmpty()) {
            // TODO: 이미지 업로드 로직 구현
            // String imageUrl = imageUploadService.uploadImage(profileImage, "dog/" + dogId);
            // dogMapper.updateProfileImage(dogId, imageUrl, userId);
            log.info("프로필 이미지 업로드 예정 - dogId: {}", dogId);
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
            throw new IllegalArgumentException("존재하지 않는 반려견입니다: " + dogId);
        }

        return dog;
    }

    /**
     * 본인의 반려견 리스트 조회
     * @param userId 사용자 ID
     * @return 반려견 리스트
     */
    public List<DogResponse> getMyDogs(Long userId) {
        log.info("본인 반려견 리스트 조회 - userId: {}", userId);
        return dogMapper.selectDogsByUserId(userId);
    }

    /**
     * 타인의 반려견 리스트 조회
     * @param username 사용자명
     * @return 반려견 리스트
     */
    public List<DogResponse> getUserDogs(String username) {
        log.info("타인 반려견 리스트 조회 - username: {}", username);
        return dogMapper.selectDogsByUsername(username);
    }

    /**
     * 반려견 정보 수정
     * @param userId 사용자 ID
     * @param dogId 반려견 ID
     * @param request 수정 요청 정보
     * @param profileImage 프로필 이미지 (선택)
     */
    @Transactional
    public void updateDog(Long userId, Long dogId, DogUpdateRequest request, MultipartFile profileImage) {
        log.info("반려견 정보 수정 요청 - userId: {}, dogId: {}", userId, dogId);

        // 소유자 확인
        DogResponse dog = dogMapper.selectDogByIdAndUserId(dogId, userId);
        if (dog == null) {
            throw new IllegalArgumentException("수정 권한이 없거나 존재하지 않는 반려견입니다");
        }

        // 이름 변경 시 중복 확인
        if (request.getName() != null && !request.getName().equals(dog.getName())) {
            if (dogMapper.existsByUserIdAndName(userId, request.getName())) {
                throw new IllegalArgumentException("이미 등록된 반려견 이름입니다: " + request.getName());
            }
        }

        // 반려견 정보 수정
        int result = dogMapper.updateDog(dogId, userId, request, userId);
        if (result == 0) {
            throw new RuntimeException("반려견 정보 수정에 실패했습니다");
        }

        // 프로필 이미지 업데이트 (선택사항)
        if (profileImage != null && !profileImage.isEmpty()) {
            // TODO: 이미지 업로드 로직 구현
            // String imageUrl = imageUploadService.uploadImage(profileImage, "dog/" + dogId);
            // dogMapper.updateProfileImage(dogId, imageUrl, userId);
            log.info("프로필 이미지 업데이트 예정 - dogId: {}", dogId);
        }

        log.info("반려견 정보 수정 완료 - dogId: {}", dogId);
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

        // 소프트 삭제
        int result = dogMapper.deleteDog(dogId, userId, userId);
        if (result == 0) {
            throw new RuntimeException("반려견 정보 삭제에 실패했습니다");
        }

        log.info("반려견 삭제 완료 - dogId: {}", dogId);
    }
}