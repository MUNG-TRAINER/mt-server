package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.exception.InvalidInputException;
import com.mungtrainer.mtserver.counseling.dao.CounselingDao;
import com.mungtrainer.mtserver.counseling.dto.request.CounselingPostRequestDTO;
import com.mungtrainer.mtserver.counseling.dto.request.CreateCounselingRequestDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CancelCounselingResponseDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponseDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CounselingPostResponseDTO;
import com.mungtrainer.mtserver.counseling.dto.response.CreateCounselingResponseDTO;
import com.mungtrainer.mtserver.counseling.entity.Counseling;
import com.mungtrainer.mtserver.common.exception.CounselingCreateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselingService {
    private final CounselingDao counselingDao;
//    private final DogDao dogDao; // 반려견 DAO 주입

    public CreateCounselingResponseDTO createCounseling(CreateCounselingRequestDTO requestDto, Long userId){

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

        return new CreateCounselingResponseDTO(counseling.getCounselingId(), "상담 신청이 완료되었습니다");
    }


    /**
     * 상담 취소
     * @param counselingId 취소할 상담 ID
     * @return 취소 성공 여부 메시지
     */
    public CancelCounselingResponseDTO cancelCounseling(Long counselingId, Long userId) {
        // 1. 상담 조회
        Counseling counseling = counselingDao.findById(counselingId);

        if (counseling == null) {
            return new CancelCounselingResponseDTO(false, "이미 취소된 상담이거나 존재하지 않는 상담입니다.");
        }

        // 2. 권한 체크
        if (!counseling.getCreatedBy().equals(userId)) {
            return new CancelCounselingResponseDTO(false, "해당 상담을 취소할 권한이 없습니다.");
        }

        // 3. 취소 처리
        int result = counselingDao.cancelCounseling(counselingId);

        if (result == 0) {
            return new CancelCounselingResponseDTO(false, "상담 취소에 실패했습니다.");
        } else {
            return new CancelCounselingResponseDTO(true, "상담이 성공적으로 취소되었습니다.");
        }
    }

    // <============ (훈련사) 상담 완료 전 후 반려견 리스트 조회 ==============>
    public List<CounselingDogResponseDTO> getDogsByCompleted(boolean completed){
        return counselingDao.findDogsByCompleted(completed);
    }


    // <============ (훈련사) 상담 내용 작성 ==============>
    public CounselingPostResponseDTO addCounselingContent(
            Long counselingId,
            CounselingPostRequestDTO requestDto,
            Long trainerId
    ) {

        // 1. 상담 존재 여부 & 완료 여부 확인
        Counseling counseling = counselingDao.findById(counselingId);
        if (counseling == null) {
            return new CounselingPostResponseDTO(false, "존재하지 않는 상담입니다.");
        }

        if (Boolean.TRUE.equals(counseling.getIsCompleted())) {
            return new CounselingPostResponseDTO(false, "이미 완료된 상담입니다.");
        }

        // 2. 상담 내용 업데이트 + 완료 처리
        int updatedRows = counselingDao.updateContentAndComplete(counselingId, requestDto.getContent(), trainerId);
        if (updatedRows == 0) {
            return new CounselingPostResponseDTO(false, "상담 내용 저장에 실패했습니다.");
        }

        return new CounselingPostResponseDTO(true, "상담 내용이 저장되었습니다.");
    }



}
