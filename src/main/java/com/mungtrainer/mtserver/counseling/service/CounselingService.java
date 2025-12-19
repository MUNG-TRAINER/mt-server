package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.exception.InvalidInputException;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.CounselingDAO;
import com.mungtrainer.mtserver.counseling.dto.request.CounselingPostRequest;
import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequest;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponse;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponse;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingPostResponse;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponse;
import com.mungtrainer.mtserver.counseling.entity.Counseling;
import com.mungtrainer.mtserver.common.exception.CounselingCreateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselingService {
    private final CounselingDAO counselingDao;
    private final S3Service s3Service;
//    private final DogDao dogDao; // 반려견 DAO 주입

    public CreateCounselingResponse createCounseling(CreateCounselingRequest requestDto, Long userId){

        // DogId 존재여부 확인
//     if (!dogDao.existsById(requestDto.getDogId())) {
//        throw new EntityNotFoundException("해당 반려견이 존재하지 않습니다.");
//    }

//    if (!dogDao.isOwnedByUser(requestDto.getDogId(), userId)) {
//        throw new UnauthorizedAccessException("해당 반려견에 대한 권한이 없습니다.");
//    }

        if (!requestDto.getPhone().matches("^\\d{2,3}-\\d{3,4}-\\d{4}$")) {
            throw new InvalidInputException("전화번호 형식이 올바르지 않습니다.");
        }

        if (requestDto.getDogId() == null) {
            throw new InvalidInputException("반려견 ID는 필수입니다.");
        }

        Counseling counseling = Counseling.builder()
                .dogId(requestDto.getDogId())
                .phone(requestDto.getPhone())
                .isCompleted(false) // 상담 완료 전
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        int result = counselingDao.insertCounseling(counseling);

        if (result == 0) {
            throw new CounselingCreateException("상담 신청에 실패했습니다.");
        }

        return new CreateCounselingResponse(counseling.getCounselingId(), "상담 신청이 완료되었습니다");
    }


    /**
     * 상담 취소
     * @param counselingId 취소할 상담 ID
     * @return 취소 성공 여부 메시지
     */
    public CancelCounselingResponse cancelCounseling(Long counselingId, Long userId) {
        // 1. 상담 조회
        Counseling counseling = counselingDao.findById(counselingId);

        if (counseling == null) {
            return new CancelCounselingResponse(false, "이미 취소된 상담이거나 존재하지 않는 상담입니다.");
        }

        // 2. 권한 체크
        if (!counseling.getCreatedBy().equals(userId)) {
            return new CancelCounselingResponse(false, "해당 상담을 취소할 권한이 없습니다.");
        }

        // 3. 취소 처리
        int result = counselingDao.cancelCounseling(counselingId);

        if (result == 0) {
            return new CancelCounselingResponse(false, "상담 취소에 실패했습니다.");
        } else {
            return new CancelCounselingResponse(true, "상담이 성공적으로 취소되었습니다.");
        }
    }

    // <============ (훈련사) 상담 완료 전 후 반려견 리스트 조회 ==============>
    public List<CounselingDogResponse> getDogsByCompleted(boolean completed) {
        List<CounselingDogResponse> dogs = counselingDao.findDogsByCompleted(completed);

        // S3 키를 Presigned URL로 변환
        return dogs.stream()
                .map(dog -> {
                    String presignedUrl = null;
                    if (dog.getDogImage() != null && !dog.getDogImage().isEmpty()) {
                        try {
                            presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getDogImage());
                            log.debug("Presigned URL 생성 완료 - key: {}", dog.getDogImage());
                        } catch (Exception e) {
                            log.error("Presigned URL 생성 실패 - key: {}, error: {}", dog.getDogImage(), e.getMessage());
                            // 실패해도 null로 처리하여 다른 데이터는 정상 반환
                        }
                    }
                    return new CounselingDogResponse(
                            dog.getCounselingId(),
                            dog.getDogId(),
                            dog.getDogName(),
                            dog.getOwnerName(),
                            presignedUrl
                    );
                })
                .collect(Collectors.toList());
    }


    // <============ (훈련사) 상담 내용 작성 ==============>
    public CounselingPostResponse addCounselingContent(
            Long counselingId,
            CounselingPostRequest requestDto,
            Long trainerId
    ) {

        // 1. 상담 존재 여부 & 완료 여부 확인
        Counseling counseling = counselingDao.findById(counselingId);
        if (counseling == null) {
            return new CounselingPostResponse(false, "존재하지 않는 상담입니다.");
        }

        if (Boolean.TRUE.equals(counseling.getIsCompleted())) {
            return new CounselingPostResponse(false, "이미 완료된 상담입니다.");
        }

        // 2. 상담 내용 업데이트 + 완료 처리
        int updatedRows = counselingDao.updateContentAndComplete(counselingId, requestDto.getContent(), trainerId);
        if (updatedRows == 0) {
            return new CounselingPostResponse(false, "상담 내용 저장에 실패했습니다.");
        }

        return new CounselingPostResponse(true, "상담 내용이 저장되었습니다.");
    }



}
