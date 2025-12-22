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
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 상태 갱신용 private 메서드
    private void updateApplicationStatusIfExpired(TrainingCourseApplication app) {
        TrainingSession session = applicationDao.findSessionById(app.getSessionId());
        LocalDateTime sessionEnd = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        // 신청 상태 만료 처리
        if ("DONE".equals(session.getStatus()) &&
                Arrays.asList("APPLIED", "WAITING", "COUNSELING_REQUIRED", "ACCEPT").contains(app.getStatus()) &&
                !"EXPIRED".equals(app.getStatus())) {   // 이미 EXPIRED면 건너뜀
            applicationDao.updateApplicationStatusIfNotExpired(app.getApplicationId(), "EXPIRED");
            app.setStatus("EXPIRED");
        }
    }
    @Transactional
    // 신청 리스트 조회
    public List<ApplicationResponse> getApplicationsByUserId(Long userId) {
        List<TrainingCourseApplication> applicationList = applicationDao.findByUserId(userId);
        // 상태 갱신
        for (TrainingCourseApplication app : applicationList) {
            updateApplicationStatusIfExpired(app);
        }
        return applicationList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 신청내역 리스트 (카드용)
    public List<ApplicationListViewResponse> getApplicationListView(Long userId) {
        // 상태 갱신
        List<TrainingCourseApplication> applications = applicationDao.findByUserId(userId);

        for (TrainingCourseApplication app : applications) {
            updateApplicationStatusIfExpired(app);
        }

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
    public List<ApplicationResponse> applyCourse(Long userId, Long courseId, ApplicationRequest request) {
        // 1. 해당 강아지가 userId 소유인지 확인
        Long ownerId = applicationDao.findOwnerByDogId(request.getDogId());
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        // 2. 코스에 속한 세션 조회
        List<TrainingSession> sessions = applicationDao.findSessionsByCourseId(courseId);
        if (sessions.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        List<ApplicationResponse> createdApplications = new ArrayList<>();

        // 3. 각 세션별 신청 처리
        for (TrainingSession session : sessions) {
            Long sessionId = session.getSessionId();

            // 중복 신청 체크
            boolean exists = applicationDao.existsByDogAndSession(request.getDogId(), sessionId);
            if (exists) {
                throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
            }

            // 세션 정원 및 현재 신청 인원
            int maxStudent = applicationDao.getMaxStudentsBySessionId(sessionId);
            int currentCount = applicationDao.countApplicationBySessionId(sessionId);
            boolean hasCounselingCompleted = applicationDao.findCounselingByDogID(request.getDogId());

            String status;
            if (currentCount >= maxStudent) {
                status = "WAITING";
            } else if (!hasCounselingCompleted) {
                status = "COUNSELING_REQUIRED";
            } else {
                status = "APPLIED";
            }

            // 신청 엔티티 생성
            TrainingCourseApplication created = TrainingCourseApplication.builder()
                    .sessionId(sessionId)
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

            // 대기 테이블 추가
            if ("WAITING".equals(status)) {
                applicationDao.insertWaiting(created.getApplicationId(), userId);
            }

            createdApplications.add(toResponse(created)); // 기존에 쓰던 변환 메서드
        }

        // 4. 장바구니 상태 업데이트 (선택적)
        if (request.getWishlistItemId() != null) {
            applicationDao.updateWishlistDetailStatus(request.getWishlistItemId(), "ORDERED");
        }

        return createdApplications;
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
    @Transactional
    public void cancelApplicationsByCourses(
            Long userId,
            ApplicationCancelRequest request
    ) {
        List<Long> courseIds = request.getCourseIds();

        if (courseIds == null || courseIds.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 1️⃣ 코스 기준으로 취소 가능한 application 조회
        List<TrainingCourseApplication> apps =
                applicationDao.findCancelableApplicationsByUserAndCourses(
                        userId, courseIds
                );

        if (apps.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 2️⃣ session 단위 그룹핑 (대기자 승격용)
        Map<Long, List<Long>> sessionMap = new HashMap<>();

        for (TrainingCourseApplication app : apps) {
            sessionMap
                    .computeIfAbsent(app.getSessionId(), k -> new ArrayList<>())
                    .add(app.getApplicationId());
        }

        // 3️⃣ 세션별 취소 + 대기자 승격
        for (Map.Entry<Long, List<Long>> entry : sessionMap.entrySet()) {
            Long sessionId = entry.getKey();
            List<Long> applicationIds = entry.getValue();

            // 신청 취소
            applicationDao.updateApplicationStatusBatch(applicationIds, "CANCELLED");
            applicationDao.updateWaitingStatusBatch(applicationIds, "CANCELLED");

            // 대기자 승격
            List<Long> waitingList =
                    applicationDao.findWaitingBySessionId(sessionId);

            for (int i = 0; i < applicationIds.size() && i < waitingList.size(); i++) {
                Long nextId = waitingList.get(i);
                applicationDao.updateApplicationStatus(nextId, "APPLIED");
                applicationDao.updateWaitingStatus(nextId, "ENTERED");
            }
        }
    }
    }

