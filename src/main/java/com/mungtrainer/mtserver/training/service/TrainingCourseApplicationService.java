package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.training.dao.ApplicationDao;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingCourseApplicationService {

    private final ApplicationDao applicationDao;

    // 엔티티를 dto로 변환
    private ApplicationResponse toResponse(TrainingCourseApplication application) {
        return ApplicationResponse.builder()
                .applicationId(application.getApplicationId())
                .sessionId(application.getSessionId())
                .dogId(application.getDogId())
                .appliedAt(application.getAppliedAt())
                .status(application.getStatus())
                .rejectReason(application.getRejectReason())
                .build();
    }

    // 신청 리스트 조회
    public List<ApplicationResponse> getApplicationsByUserId(Long userId) {
        List<TrainingCourseApplication> applicationList = applicationDao.findByUserId(userId);
        return applicationList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 신청 상세 조회
    public ApplicationResponse getApplicationById(Long userId,Long applicationId) {
        TrainingCourseApplication application = applicationDao.findById(applicationId);
        if (application == null){
              throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        if (!application.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }
        return toResponse(application);
    }

    // 신청 생성
    public ApplicationResponse createApplication(Long userId,ApplicationRequest request) {
        // 해당 사용자 인증
        Long ownerId = applicationDao.findOwnerByDogId(request.getDogId());
        if(ownerId == null || !ownerId.equals(userId)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

//        //  중복 신청 체크
//        boolean exists = applicationDao.existsByDogAndSession(request.getDogId(), request.getSessionId());
//        if (exists) {
//            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
//        }

        // 세션 정원조회 및 상태 변경
        int maxStudent = applicationDao.getMaxStudentsBySessionId(request.getSessionId());
        int currentCount = applicationDao.countApplicationBySessionId(request.getSessionId());
        String status;
        if(currentCount>=maxStudent){
            status="WAITING";
        }else {
            status="APPLIED";
        }

        TrainingCourseApplication created = TrainingCourseApplication.builder()
                .sessionId(request.getSessionId())
                .dogId(request.getDogId())
                .appliedAt(LocalDateTime.now())
                .status(status)
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        int rows = applicationDao.insertApplication(created);
        if (rows != 1) {
            throw new CustomException(ErrorCode.APPLICATION_CREATION_FAILED);
        }
        // 웨이팅이면 대기테이블에 추가
        if("WAITING".equals(status)){
            applicationDao.insertWaiting(created.getApplicationId(),userId);
        }
        return toResponse(created);
    }

    // 신청 취소
    public void cancelApplication(Long userId, Long applicationId) {
        TrainingCourseApplication application = applicationDao.findById(applicationId);
        // null이면 조회불가 에러
        if (application == null) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        // 본인만 취소 가능
        if (!application.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }
        // 신청 취소
        applicationDao.updateApplicationStatus(applicationId, "CANCELLED");

        // 4) 대기자 목록 조회
        Long sessionId = application.getSessionId();
        List<Long> waitingList = applicationDao.findWaitingBySessionId(sessionId);

        // 5) 대기자 승격
        if (waitingList != null && !waitingList.isEmpty()) {
            Long nextApplicationId = waitingList.get(0); // 가장 오래 대기한 applicationId

            // application 테이블 상태 변경 (대기 → 신청됨)
            applicationDao.updateApplicationStatus(nextApplicationId, "APPLIED");

            // waiting 테이블 상태 변경 (WAITING → ENTERED)
            applicationDao.updateWaitingStatus(nextApplicationId, "ENTERED");
        }

        }

    }
