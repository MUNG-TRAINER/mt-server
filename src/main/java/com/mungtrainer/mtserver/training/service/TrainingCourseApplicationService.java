package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dto.request.ApplicationCancelRequest;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationListViewResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationStatusResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingCourseApplicationService {

    private final ApplicationDAO applicationDao;
    private final S3Service s3Service;

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

    // 신청내역 리스트 (카드용)
    public List<ApplicationListViewResponse> getApplicationListView(Long userId) {
        // 1. DAO에서 리스트 조회
        List<ApplicationListViewResponse> list = applicationDao.findApplicationListViewByUserId(userId);

        if (list == null || list.isEmpty()) return Collections.emptyList();

        // 2. S3 key 수집
        List<String> imageKeys = list.stream()
                .map(ApplicationListViewResponse::getMainImage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 3~4. Presigned URL 발급 + key → URL 매핑 안전하게
        Map<String, String> imageUrlMap = new HashMap<>();
        if (!imageKeys.isEmpty()) {
            for (String key : imageKeys) {
                List<String> urls = s3Service.generateDownloadPresignedUrls(Collections.singletonList(key));
                if (urls != null && !urls.isEmpty() && urls.get(0) != null && !urls.get(0).isEmpty()) {
                    imageUrlMap.put(key, urls.get(0));
                }
            }
        }
        // 5. toBuilder() 사용해서 새 DTO 생성 + URL 매핑
        List<ApplicationListViewResponse> mappedList = list.stream()
                .map(dto -> {
                    if (dto.getMainImage() != null) {
                        return dto.toBuilder()
                                .mainImage(imageUrlMap.get(dto.getMainImage()))
                                .build();
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return mappedList;
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
    // status 조회 ui용
    public ApplicationStatusResponse getApplicationStatus(
            Long userId, Long applicationId
    ) {
        TrainingCourseApplication application =
                applicationDao.findById(applicationId);

        if (application == null) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        if (!application.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        return ApplicationStatusResponse.builder()
                .applicationId(application.getApplicationId())
                .status(application.getStatus())
                .build();
    }
    // 신청 생성
    public ApplicationResponse createApplication(Long userId,ApplicationRequest request,Long wishlistItemId) {
        // 해당 사용자 인증
        Long ownerId = applicationDao.findOwnerByDogId(request.getDogId());
        if(ownerId == null || !ownerId.equals(userId)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        //  중복 신청 체크
        boolean exists = applicationDao.existsByDogAndSession(request.getDogId(), request.getSessionId());
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

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

        // 5. 장바구니에서 넘어온 경우 상태 변경
        if (wishlistItemId != null) {
            applicationDao.updateWishlistDetailStatus(wishlistItemId, "ORDERED");
        }

        // 웨이팅이면 대기테이블에 추가
        if("WAITING".equals(status)){
            applicationDao.insertWaiting(created.getApplicationId(),userId);
        }
        return toResponse(created);
    }

    // 신청 강아지 수정
    public void updateApplicationDog(Long userId, Long applicationId, Long newDogId) {
        // newDogId null 체크
        if (newDogId == null) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 1. 신청 조회
        TrainingCourseApplication application = applicationDao.findById(applicationId);
        if (application == null) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 2. 본인 신청인지 확인
        if (!application.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        // 3. 변경하려는 강아지가 동일하면 return
        if (application.getDogId() != null && application.getDogId().equals(newDogId)) {
            return;
        }

        // 4. 강아지 소유권 체크 (본인 강아지인지)
        Long ownerId = applicationDao.findOwnerByDogId(newDogId);
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        // 5. 같은 세션에 이미 신청한 강아지인지 확인 (중복 방지)
        boolean exists = applicationDao.existsByDogAndSession(newDogId, application.getSessionId());
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        // 6. 업데이트
        applicationDao.updateApplicationDog(applicationId, newDogId);
    }


    // 신청 취소
    public void cancelApplication(Long userId, Long applicationId) {
        TrainingCourseApplication application = applicationDao.findById(applicationId);
        // null이면 조회불가 에러
        if (application == null) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }
        // 본인만 취소 가능 에러
        if (!application.getCreatedBy().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }
        // 현재 해당 신청 status 확인
        String currentStatus = application.getStatus();
        // 만약 상태가 웨이팅인 경우 웨이팅 신청 취소로 상태 변경
        if ("WAITING".equals(currentStatus)) {
            applicationDao.updateWaitingStatus(applicationId, "CANCELLED");
            return;
        }

        // 신청 취소
        applicationDao.updateApplicationStatus(applicationId, "CANCELLED");

        // 대기자 목록 조회
        Long sessionId = application.getSessionId();
        List<Long> waitingList = applicationDao.findWaitingBySessionId(sessionId);

        // 대기자 신청으로 승격
        if (waitingList != null && !waitingList.isEmpty()) {
            Long nextApplicationId = waitingList.get(0); // 첫번째 대기자 applicationId

            // application 테이블 상태 변경 (대기 → 신청됨)
            applicationDao.updateApplicationStatus(nextApplicationId, "APPLIED");

            // waiting 테이블 상태 변경 (WAITING → ENTERED)
            applicationDao.updateWaitingStatus(nextApplicationId, "ENTERED");
        }
    }
    // 여러 신청 취소
    public void deleteApplicationList(Long userId, ApplicationCancelRequest request) {
        List<Long> applicationIds = request.getApplicationId();
        if(applicationIds == null || applicationIds.isEmpty()){
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 모든 신청 조회 후, 세션별로 그룹화
        Map<Long, List<Long>> sessionToApplicationIds = new HashMap<>();
        for (Long applicationId : applicationIds) {
            TrainingCourseApplication app = applicationDao.findById(applicationId);
            if (app == null) {
                throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
            }
            if (!app.getCreatedBy().equals(userId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
            }

            sessionToApplicationIds
                    .computeIfAbsent(app.getSessionId(), k -> new ArrayList<>())
                    .add(applicationId);
        }

        // 세션별 취소 처리 + 대기자 승격
        for (Map.Entry<Long, List<Long>> entry : sessionToApplicationIds.entrySet()) {
            Long sessionId = entry.getKey();
            List<Long> idsToCancel = entry.getValue();

            // 취소 처리
            for (Long id : idsToCancel) {
                TrainingCourseApplication app = applicationDao.findById(id);
                if ("WAITING".equals(app.getStatus())) {
                    // 대기 테이블 상태 변경
                    applicationDao.updateWaitingStatus(id, "CANCELLED");
                    // application 테이블 상태도 CANCELLED로 변경
                    applicationDao.updateApplicationStatus(id, "CANCELLED");
                } else {
                    applicationDao.updateApplicationStatus(id, "CANCELLED");
                }
            }

            // 대기자 승격 처리 (취소한 수만큼 승격)
            List<Long> waitingList = applicationDao.findWaitingBySessionId(sessionId);
            if (waitingList != null && !waitingList.isEmpty()) {
                int cancelCount = idsToCancel.size(); // 이번 세션에서 취소한 신청 수
                for (int i = 0; i < cancelCount && i < waitingList.size(); i++) {
                    Long nextApplicationId = waitingList.get(i);
                    // 신청 테이블 상태 변경 (대기 → 신청됨)
                    applicationDao.updateApplicationStatus(nextApplicationId, "APPLIED");
                    // waiting 테이블 상태 변경 (WAITING → ENTERED)
                    applicationDao.updateWaitingStatus(nextApplicationId, "ENTERED");
                }
            }
        }
    }
}
