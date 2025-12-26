package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingAttendanceDAO;
import com.mungtrainer.mtserver.training.dto.request.ApplicationCancelRequest;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistApplyRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationListViewResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationStatusResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingCourseApplicationService {

    private final ApplicationDAO applicationDao;
    private final S3Service s3Service;
    private final TrainerUserDAO trainerUserDao;  // ← 추가
    private final TrainingAttendanceDAO trainingAttendanceDao;  // ← 추가

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
    @Transactional
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
//            int maxStudent = applicationDao.getMaxStudentsBySessionId(sessionId);
//            int currentCount = applicationDao.countApplicationBySessionId(sessionId);

            // 상담 완료 여부만 확인 (정원은 트레이너 승인 시점에 확인)
            boolean hasCounselingCompleted = applicationDao.findCounselingByDogID(request.getDogId());

//            String status;
//            if (currentCount >= maxStudent) {
//                status = "WAITING";
//            } else if (!hasCounselingCompleted) {
//                status = "COUNSELING_REQUIRED";
//            } else {
//                status = "APPLIED";
//            }

            // 상태 결정: 상담 완료 여부만 확인
            String status;
            if (!hasCounselingCompleted) {
              status = "COUNSELING_REQUIRED";
            } else {
              status = "APPLIED";  // 정원 여부와 무관하게 모두 APPLIED
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
          // 이제 waiting 테이블 등록은 트레이너 승인 시점에 처리
//            if ("WAITING".equals(status)) {
//                applicationDao.insertWaiting(created.getApplicationId(), userId);
//            }

            createdApplications.add(toResponse(created)); // 기존에 쓰던 변환 메서드
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

//        // 대기자 신청으로 승격
//        if (waitingList != null && !waitingList.isEmpty()) {
//            Long nextApplicationId = waitingList.get(0); // 첫번째 대기자 applicationId
//
//            // application 테이블 상태 변경 (대기 → 신청됨)
//            applicationDao.updateApplicationStatus(nextApplicationId, "APPLIED");
//
//            // waiting 테이블 상태 변경 (WAITING → ENTERED)
//            applicationDao.updateWaitingStatus(nextApplicationId, "ENTERED");
//        }

      // ⭐ 대기자 자동 승격 - WAITING은 이미 트레이너가 승인한 상태이므로 ACCEPT로 바로 변경
      if (waitingList != null && !waitingList.isEmpty()) {
        Long nextApplicationId = waitingList.get(0); // 첫번째 대기자 applicationId

        // TrainerUserDAO의 새 메서드 사용 (감사 정보 제외한 단순 업데이트)
        // 주의: TrainerUserDAO를 주입받아야 합니다
        trainerUserDao.updateApplicationStatusSimple(nextApplicationId, "ACCEPT");
        trainerUserDao.updateWaitingStatus(nextApplicationId, "PROMOTED");

        // 출석 정보 생성 (TrainingAttendanceDAO 사용)
        // 주의: TrainingAttendanceDAO를 주입받아야 합니다
        try {
          trainingAttendanceDao.insertAttendanceByApplicationId(nextApplicationId, 0L);
        } catch (Exception e) {
          log.error("취소 후 대기자 승격 시 출석 정보 생성 실패 - applicationId: {}", nextApplicationId, e);
          // 출석 정보 생성 실패해도 승격은 유지
        }
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
    // wishlist에서 신청으로 가는 로직
    @Transactional
    public List<ApplicationResponse> applyWishlistCourses(Long userId, List<WishlistApplyRequest> requests) {
        List<ApplicationResponse> createdApplications = new ArrayList<>();

        for (WishlistApplyRequest req : requests) {
            Long wishlistItemId = req.getWishlistItemId();
            Long dogId = req.getDogId();
            Long courseId = req.getCourseId();

            // 1. 소유 확인
            Long ownerId = applicationDao.findOwnerByDogId(dogId);
            if (!userId.equals(ownerId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
            }

            // 2. 세션 조회
            List<TrainingSession> sessions = applicationDao.findSessionsByCourseId(courseId);
            if (sessions.isEmpty()) {
                throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
            }

            // 3. 각 세션별 신청 처리
            for (TrainingSession session : sessions) {
                Long sessionId = session.getSessionId();
                boolean exists = applicationDao.existsByDogAndSession(dogId, sessionId);
                if (exists) {
                    throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
                }
                boolean hasCounselingCompleted = applicationDao.findCounselingByDogID(dogId);
                if (!hasCounselingCompleted) {
                    throw new CustomException(ErrorCode.COUNSELING_REQUIRED);
                }
                int maxStudent = applicationDao.getMaxStudentsBySessionId(sessionId);
                int currentCount = applicationDao.countApplicationBySessionId(sessionId);


                String status;
                if (currentCount >= maxStudent) {
                    status = "WAITING";
                } else {
                    status = "APPLIED";
                }

                TrainingCourseApplication created = TrainingCourseApplication.builder()
                        .sessionId(sessionId)
                        .dogId(dogId)
                        .appliedAt(LocalDateTime.now())
                        .status(status)
                        .createdBy(userId)
                        .updatedBy(userId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                int rows = applicationDao.insertApplication(created);
                if (rows != 1) throw new CustomException(ErrorCode.APPLICATION_CREATION_FAILED);

                if ("WAITING".equals(status)) {
                    applicationDao.insertWaiting(created.getApplicationId(), userId);
                }

                createdApplications.add(toResponse(created));
            }

            // 4. 장바구니 상태 업데이트
            applicationDao.updateWishlistDetailStatus(wishlistItemId, "ORDERED");
        }

        return createdApplications;
    }
}


