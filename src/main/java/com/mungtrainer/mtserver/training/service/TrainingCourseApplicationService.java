package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
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
            throw new RuntimeException("신청 정보를 찾을 수 없습니다.");
        }
        if (!application.getCreatedBy().equals(userId)) {
            throw new RuntimeException("본인의 신청만 조회할 수 있습니다.");
        }
        return toResponse(application);
    }

    // 신청 생성
    public ApplicationResponse createApplication(Long userId,ApplicationRequest request) {
        // 1. 해당 사용자 인증
        Long ownerId = applicationDao.findOwnerByDogId(request.getDogId());
        if(ownerId == null || !ownerId.equals(userId)){
            throw new RuntimeException("본인만 신청 가능합니다.");
        }

        // 2. 중복 신청 체크
        boolean exists = applicationDao.existsByDogAndSession(request.getDogId(), request.getSessionId());
        if (exists) {
            throw new RuntimeException("이미 신청한 세션입니다.(APPLIED 상태)");
        }

        TrainingCourseApplication created = TrainingCourseApplication.builder()
                .sessionId(request.getSessionId())
                .dogId(request.getDogId())
                .appliedAt(LocalDateTime.now())
                .status("APPLIED") // 기본 상태
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        int rows = applicationDao.insertApplication(created);
        if (rows != 1) {
            throw new RuntimeException("신청 생성 실패");
        }
        return toResponse(created);
    }

    // 신청 취소
    public void cancelApplication(Long userId, Long applicationId) {
        TrainingCourseApplication application = applicationDao.findById(applicationId);
        if (application == null) {
            throw new RuntimeException("신청 정보를 찾을 수 없습니다.");
        }

        if (!application.getCreatedBy().equals(userId)) {
            throw new RuntimeException("본인의 신청만 취소할 수 있습니다.");
        }
        applicationDao.updateApplicationStatus(applicationId, "CANCELLED");
    }
}
