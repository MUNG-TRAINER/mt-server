package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.CounselingDAO;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.counseling.dto.request.CounselingPostRequest;
import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequest;
import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.counseling.entity.Counseling;
import com.mungtrainer.mtserver.dog.dao.DogDAO;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselingService {
    private final CounselingDAO counselingDao;
    private final S3Service s3Service;
    private final DogDAO dogDao;
    private final TrainerUserDAO trainerUserDao;


    public CreateCounselingResponse createCounseling(CreateCounselingRequest requestDto, Long userId){

        // 1. 반려견 존재 여부 및 소유권 확인
        DogResponse dog = dogDao.selectDogByIdAndUserId(requestDto.getDogId(), userId);
        if (dog == null) {
            throw new CustomException(ErrorCode.COUNSELING_DOG_NOT_OWNED);
        }

        // 2. 훈련사 조회
        Long trainerId = trainerUserDao.findTrainerIdByDogId(requestDto.getDogId());
        if (trainerId == null) {
            throw new CustomException(ErrorCode.TRAINER_NOT_FOUND);
        }

        Counseling counseling = Counseling.builder()
                .dogId(requestDto.getDogId())
                .phone(requestDto.getPhone())
                .isCompleted(false)
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        int result = counselingDao.insertCounseling(counseling);

        if (result == 0) {
            throw new CustomException(ErrorCode.COUNSELING_CREATE_FAILED);
        }





        return new CreateCounselingResponse(counseling.getCounselingId(), "상담 신청이 완료되었습니다");
    }


    /**
     * 상담 취소
     * @param counselingId 취소할 상담 ID
     * @param userId 사용자 ID
     * @return 취소 성공 여부 메시지
     */
    public CancelCounselingResponse cancelCounseling(Long counselingId, Long userId) {
        // 1. 상담 조회
        Counseling counseling = counselingDao.findById(counselingId);

        if (counseling == null) {
            throw new CustomException(ErrorCode.COUNSELING_ALREADY_DELETED);
        }

        // 2. 권한 체크
        if (!counseling.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.COUNSELING_NO_PERMISSION);
        }

        // 3. 취소 처리
        int result = counselingDao.cancelCounseling(counselingId);

        if (result == 0) {
            throw new CustomException(ErrorCode.COUNSELING_CANCEL_FAILED);
        }

        return new CancelCounselingResponse(true, "상담이 성공적으로 취소되었습니다.");
    }

    /**
     * (훈련사) 상담 완료 전후 반려견 리스트 조회
     * @param completed 상담 완료 여부
     * @param trainerId 훈련사 ID
     * @return 상담 반려견 목록
     */
    public List<CounselingDogResponse> getDogsByCompleted(boolean completed, Long trainerId) {
        // 1. DB에서 반려견 리스트 조회 (훈련사가 관리하는 훈련 과정에 신청한 반려견만)
        List<CounselingDogResponse> dogs = counselingDao.findDogsByCompleted(completed, trainerId);

        if (dogs.isEmpty()) {
            return List.of();
        }

        // 2. 모든 반려견의 S3 키 추출 (null이나 빈 문자열 제외)
        List<String> imageKeys = dogs.stream()
                .map(CounselingDogResponse::getDogImage)
                .filter(key -> key != null && !key.isEmpty())
                .collect(Collectors.toList());

        // 3. S3 Presigned URL 일괄 발급
        List<String> presignedUrls = s3Service.generateDownloadPresignedUrls(imageKeys);

        // 4. 각 반려견 객체에 URL 매핑
        int urlIndex = 0;
        for (CounselingDogResponse dog : dogs) {
            if (dog.getDogImage() != null && !dog.getDogImage().isEmpty()) {
                dog.setDogImage(presignedUrls.get(urlIndex++));
            }
        }

        return dogs;
    }

    /**
     * (훈련사) 상담 내용 작성
     * @param counselingId 상담 ID
     * @param requestDto 상담 내용
     * @param trainerId 훈련사 ID
     * @return 작성 성공 여부
     */
    @Transactional
    // <============ (훈련사) 상담 내용 작성 ==============>
    public CounselingPostResponse addCounselingContent(
            Long counselingId,
            CounselingPostRequest requestDto,
            Long trainerId
    ) {

        // 1. 상담 존재 여부 확인
        Counseling counseling = counselingDao.findById(counselingId);
        if (counseling == null) {
            throw new CustomException(ErrorCode.COUNSELING_NOT_FOUND);
        }

        // 2. 완료 여부 확인
        if (Boolean.TRUE.equals(counseling.getIsCompleted())) {
            throw new CustomException(ErrorCode.COUNSELING_ALREADY_COMPLETED);
        }

        // 3. 훈련사 권한 확인 - 해당 상담의 반려견 소유자와 연결되어 있는지 검증
        Long dogOwnerId = dogDao.getUserIdByDogId(counseling.getDogId());
        if (dogOwnerId == null) {
            throw new CustomException(ErrorCode.COUNSELING_NOT_FOUND);
        }


        boolean hasPermission = trainerUserDao.existsTrainerUserRelation(trainerId, dogOwnerId);
        if (!hasPermission) {
            throw new CustomException(ErrorCode.COUNSELING_TRAINER_NO_PERMISSION);
        }

        // 4. 상담 내용 업데이트 + 완료 처리
        int updatedRows = counselingDao.updateContentAndComplete(counselingId, requestDto.getContent(), trainerId);
        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.COUNSELING_UPDATE_FAILED);
        }

        // 5. 연관된 훈련 신청 상태 변경
        counselingDao.updateApplicationStatusAfterCounseling(trainerId, counseling.getDogId());


        return new CounselingPostResponse(true, "상담 내용이 저장되었습니다.");
    }

    /**
     * 상담 신청용 반려견 정보 조회
     * @param dogId 반려견 ID
     * @param userId 사용자 ID
     * @return 반려견 정보 + 상담 신청 여부
     */
    public DogForCounselingResponse getDogForCounseling(Long dogId, Long userId) {
        // 1. DB에서 반려견 정보 + 상담 신청 여부 조회
        DogForCounselingResponse dog = counselingDao.findDogForCounseling(dogId, userId);

        if (dog == null) {
            throw new CustomException(ErrorCode.COUNSELING_DOG_NOT_OWNED);
        }

        // 2. 반려견 이미지가 있는 경우 S3 Presigned URL 발급
        if (dog.getProfileImage() != null && !dog.getProfileImage().isEmpty()) {
            String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
            dog.setProfileImage(presignedUrl);
        }

        return dog;
    }

    /**
     * 사용자의 상담 목록 조회
     * @param userId 사용자 ID
     * @return 상담 목록
     */
    public List<UserCounselingListResponse> getUserCounselings(Long userId) {
        // 1. DB에서 상담 목록 조회
        List<UserCounselingListResponse> counselings = counselingDao.findCounselingsByUserId(userId);

        if (counselings.isEmpty()) {
            return List.of();
        }

        // 2. 모든 반려견의 S3 키 추출 (null이나 빈 문자열 제외)
        List<String> imageKeys = counselings.stream()
                .map(UserCounselingListResponse::getDogImage)
                .filter(key -> key != null && !key.isEmpty())
                .collect(Collectors.toList());

        if (!imageKeys.isEmpty()) {
            // 3. S3 Presigned URL 일괄 발급
            List<String> presignedUrls = s3Service.generateDownloadPresignedUrls(imageKeys);

            // 4. 각 상담 객체에 URL 매핑
            int urlIndex = 0;
            for (UserCounselingListResponse counseling : counselings) {
                if (counseling.getDogImage() != null && !counseling.getDogImage().isEmpty()) {
                    counseling.setDogImage(presignedUrls.get(urlIndex++));
                }
            }
        }

        return counselings;
    }

    /**
     * 사용자의 특정 상담 상세 조회
     * @param counselingId 상담 ID
     * @param userId 사용자 ID
     * @return 상담 상세 정보
     */
    public UserCounselingDetailResponse getUserCounselingDetail(Long counselingId, Long userId) {
        // 1. DB에서 상담 상세 조회
        UserCounselingDetailResponse counseling = counselingDao.findCounselingDetailById(counselingId, userId);

        if (counseling == null) {
            throw new CustomException(ErrorCode.COUNSELING_DETAIL_NOT_FOUND);
        }

        // 2. 반려견 이미지가 있는 경우 S3 Presigned URL 발급
        if (counseling.getDogImage() != null && !counseling.getDogImage().isEmpty()) {
            String presignedUrl = s3Service.generateDownloadPresignedUrl(counseling.getDogImage());
            counseling.setDogImage(presignedUrl);
        }

        return counseling;
    }

}
