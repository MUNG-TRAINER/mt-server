package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingAttendanceDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import com.mungtrainer.mtserver.training.dto.request.ApplicationCancelRequest;
import com.mungtrainer.mtserver.training.dto.request.ApplicationRequest;
import com.mungtrainer.mtserver.order.dto.request.WishlistApplyRequest;
import com.mungtrainer.mtserver.training.dto.response.ApplicationListViewResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationRawData;
import com.mungtrainer.mtserver.training.dto.response.ApplicationResponse;
import com.mungtrainer.mtserver.training.dto.response.ApplicationStatusResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourseApplication;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingCourseApplicationService {

    private final ApplicationDAO applicationDao;
    private final S3Service s3Service;
    private final TrainerUserDAO trainerUserDao;
    private final TrainingAttendanceDAO trainingAttendanceDao;
    private final TrainingSessionDAO trainingSessionDao;

    /**
     * 수업 시작 마감 시간 (시간)
     * application.yml의 session.deadline.hours 값 사용
     * 기본값: 24시간
     */
    @Value("${session.deadline.hours:24}")
    private int sessionDeadlineHours;

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

        // 1. DAO에서 세션별 Raw Data 조회
        List<ApplicationRawData> rawDataList = applicationDao.findApplicationListViewByUserId(userId);
        if (rawDataList == null || rawDataList.isEmpty()) return Collections.emptyList();

        // 2. 과정(courseId + dogId) 단위로 그룹핑
        Map<String, List<ApplicationRawData>> groupedByCourseAndDog = rawDataList.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getCourseId() + "_" + data.getDogId(),
                        LinkedHashMap::new, // 순서 유지
                        Collectors.toList()
                ));

        // 3. S3 key 수집
        List<String> imageKeys = rawDataList.stream()
                .map(ApplicationRawData::getMainImage)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 4. Presigned URL 발급 + key → URL 매핑
        Map<String, String> imageUrlMap = new HashMap<>();
        if (!imageKeys.isEmpty()) {
            for (String key : imageKeys) {
                List<String> urls = s3Service.generateDownloadPresignedUrls(Collections.singletonList(key));
                if (urls != null && !urls.isEmpty() && urls.get(0) != null && !urls.get(0).isEmpty()) {
                    imageUrlMap.put(key, urls.get(0));
                }
            }
        }

        // 5. 과정 단위로 ApplicationListViewResponse 생성
        List<ApplicationListViewResponse> result = groupedByCourseAndDog.values().stream()
                .map(courseApplications -> {
                    // 첫 번째 항목에서 과정 정보 추출
                    ApplicationRawData first = courseApplications.get(0);

                    // ApplicationItem 리스트 생성
                    List<ApplicationListViewResponse.ApplicationItem> items = courseApplications.stream()
                            .map(data -> ApplicationListViewResponse.ApplicationItem.builder()
                                    .applicationId(data.getApplicationId())
                                    .applicationStatus(data.getApplicationStatus())
                                    .price(data.getPrice())
                                    .sessionSchedule(data.getSessionSchedule())
                                    .rejectReason(data.getRejectReason())
                                    .isWaiting(data.getIsWaiting())
                                    .waitingOrder(data.getWaitingOrder())
                                    .isPreApproved(data.getIsPreApproved())
                                    .build())
                            .collect(Collectors.toList());

                    // 총 금액 계산
                    Long totalAmount = courseApplications.stream()
                            .mapToLong(data -> data.getPrice() != null ? data.getPrice() : 0L)
                            .sum();

                    // 세션 날짜 범위 계산
                    String sessionSchedule;
                    if (courseApplications.size() == 1) {
                        // 1회차: "2025-12-20 ~ 2025-12-20" (시작일 = 종료일)
                        String dateOnly = first.getSessionSchedule().substring(0, 10); // "2025-12-20"
                        sessionSchedule = dateOnly + " ~ " + dateOnly;
                    } else {
                        // 다회차: "2025-12-20 ~ 2025-12-27" (첫 세션 날짜 ~ 마지막 세션 날짜)
                        ApplicationRawData last = courseApplications.get(courseApplications.size() - 1);
                        String firstDate = first.getSessionSchedule().substring(0, 10); // "2025-12-20"
                        String lastDate = last.getSessionSchedule().substring(0, 10);   // "2025-12-27"
                        sessionSchedule = firstDate + " ~ " + lastDate;
                    }

                    // 거절 사유 수집 (있는 경우만)
                    String rejectReason = courseApplications.stream()
                            .map(ApplicationRawData::getRejectReason)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    // ApplicationListViewResponse 생성
                    return ApplicationListViewResponse.builder()
                            .courseId(first.getCourseId())
                            .tags(first.getTags())
                            .title(first.getTitle())
                            .description(first.getDescription())
                            .mainImage(first.getMainImage() != null ? imageUrlMap.get(first.getMainImage()) : null)
                            .location(first.getLocation())
                            .lessonForm(first.getLessonForm())
                            .type(first.getType())
                            .totalAmount(totalAmount)
                            .sessionSchedule(sessionSchedule)
                            .rejectReason(rejectReason.isEmpty() ? null : rejectReason)
                            .dogName(first.getDogName())
                            .dogId(first.getDogId())
                            .applicationItems(items)
                            .build();
                })
                .collect(Collectors.toList());

        return result;
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

        // 2. 과정 상태 확인 - 진행중이거나 종료된 과정은 신청 불가
        String courseStatus = applicationDao.getCourseStatusById(courseId);
        if (courseStatus == null) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }
        if ("IN_PROGRESS".equals(courseStatus)) {
            throw new CustomException(ErrorCode.COURSE_ALREADY_STARTED);
        }
        if ("DONE".equals(courseStatus)) {
            throw new CustomException(ErrorCode.COURSE_ALREADY_COMPLETED);
        }
        if ("CANCELLED".equals(courseStatus)) {
            throw new CustomException(ErrorCode.COURSE_CANCELLED);
        }

        // 3. 코스에 속한 세션 조회
        List<TrainingSession> sessions = applicationDao.findSessionsByCourseId(courseId);
        if (sessions.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        List<ApplicationResponse> createdApplications = new ArrayList<>();

        // 4. 각 세션별 신청 처리
        for (TrainingSession session : sessions) {
            Long sessionId = session.getSessionId();

            // ========================================
            // 수업 시작 마감 시간 검증
            // ========================================
            LocalDateTime sessionStart = LocalDateTime.of(
                session.getSessionDate(),
                session.getStartTime()
            );
            LocalDateTime deadline = sessionStart.minusHours(sessionDeadlineHours);

            if (LocalDateTime.now().isAfter(deadline)) {
                // 다회차의 경우 일부 회차만 마감되어도 전체 신청 불가
                log.warn("수업 마감 시간 초과 - 회차: {}, 마감 시간: {}",
                    session.getSessionNo(),
                    deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                throw new CustomException(ErrorCode.SESSION_DEADLINE_PASSED);
            }
            // ========================================

            // 중복 신청 체크
            boolean exists = applicationDao.existsByDogAndSession(request.getDogId(), sessionId);
            if (exists) {
                throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
            }

            // 세션 정원 및 현재 승인된 신청 인원 확인 (ACCEPTED, PAID만 카운트)
            int maxStudent = session.getMaxStudents(); // 세션 엔티티에서 직접 가져오기
            int currentAcceptedCount = applicationDao.countAcceptedApplications(sessionId);

            // 상담 완료 여부 확인
            boolean hasCounselingCompleted = applicationDao.findCounselingByDogID(request.getDogId());

            // 상태 결정 로직
            String status;
            if (currentAcceptedCount >= maxStudent) {
                // 정원 초과 -> WAITING
                status = "WAITING";
            } else if (!hasCounselingCompleted) {
                // 상담 미완료 -> COUNSELING_REQUIRED
                status = "COUNSELING_REQUIRED";
            } else {
                // 정원 여유 + 상담 완료 -> APPLIED
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

            // WAITING 상태일 경우 waiting 테이블에 등록
            if ("WAITING".equals(status)) {
                applicationDao.insertWaiting(created.getApplicationId(), userId);
            }

            createdApplications.add(toResponse(created));
        }

        return createdApplications;
    }

    /**
     * 신청 취소 및 대기자 자동 승격
     *
     * 트랜잭션: 이 메서드는 @Transactional이므로 예외 발생 시 전체 롤백됩니다.
     *             대기자 승격 시 출석 정보 생성이 필수이므로, 실패 시 취소도 함께 롤백됩니다.
     */
    @Transactional
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
        // 신청 취소
        applicationDao.updateApplicationStatus(applicationId, "CANCELLED");

        // WAITING 상태였다면 waiting 테이블도 업데이트
        String currentStatus = application.getStatus();
        if ("WAITING".equals(currentStatus)) {
            applicationDao.updateWaitingStatus(applicationId, "CANCELLED");
            // WAITING 상태 취소는 대기자 승격 없이 종료
            return;
        }


        // 대기자 목록 조회
        Long sessionId = application.getSessionId();
        List<Long> waitingList = applicationDao.findWaitingBySessionId(sessionId);

      // 대기자 자동 승격 - WAITING은 이미 트레이너가 승인한 상태이므로 ACCEPT로 바로 변경
      if (waitingList != null && !waitingList.isEmpty()) {
        Long nextApplicationId = waitingList.get(0); // 첫번째 대기자 applicationId

        // 1. 신청 상태 변경: WAITING → ACCEPT
        trainerUserDao.updateApplicationStatusSimple(nextApplicationId, "ACCEPT");
        trainerUserDao.updateWaitingStatus(nextApplicationId, "PROMOTED");

        // 2. 출석 정보 생성 (필수)
        // 중요: 출석 정보가 없으면 이후 출석 관리, 수료 처리 등 비즈니스 로직에서
        //          데이터 불일치 문제가 발생합니다.
        //          따라서 출석 정보 생성 실패 시 CustomException을 발생시켜
        //          전체 트랜잭션을 롤백합니다 (취소도 함께 취소됨).
        try {
          int inserted = trainingAttendanceDao.insertAttendanceByApplicationId(nextApplicationId, userId);

          if (inserted == 0) {
            log.error("출석 정보 생성 실패 (0건 삽입) - applicationId: {}", nextApplicationId);
            throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
          }

          log.info("대기자 승격 완료 - applicationId: {}, 출석 정보 생성 완료", nextApplicationId);

        } catch (CustomException e) {
          // CustomException은 그대로 던져서 트랜잭션 롤백
          throw e;
        } catch (Exception e) {
          // 기타 예외는 CustomException으로 래핑하여 트랜잭션 롤백
          log.error("취소 후 대기자 승격 시 출석 정보 생성 실패 - applicationId: {}", nextApplicationId, e);
          throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
        }
      }
    }
    /**
     * 여러 신청 일괄 취소 및 대기자 자동 승격 (applicationId 기반)
     *
     * 트랜잭션: @Transactional이므로 출석 정보 생성 실패 시 전체 롤백됩니다.
     *
     * 보안:
     * - 소유권 검증: 본인의 신청만 취소 가능
     * - 상태 검증: APPLIED, WAITING, ACCEPT 상태만 취소 가능
     */
    @Transactional
    public void cancelApplicationsByCourses(
            Long userId,
            ApplicationCancelRequest request
    ) {
        List<Long> applicationIds = request.getApplicationIds();

        if (applicationIds == null || applicationIds.isEmpty()) {
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        log.info("신청 취소 요청 - userId: {}, applicationIds: {}", userId, applicationIds);

        // 1️⃣ applicationId 기준으로 취소 가능한 application 조회
        // 소유권 및 상태 검증이 쿼리에서 처리됨 (dog.user_id = userId, status IN ('APPLIED', 'WAITING', 'ACCEPT'))
        List<TrainingCourseApplication> apps =
                applicationDao.findCancelableApplicationsByIds(
                        userId, applicationIds
                );

        if (apps.isEmpty()) {
            log.warn("취소 가능한 신청이 없음 - userId: {}, applicationIds: {}", userId, applicationIds);
            throw new CustomException(ErrorCode.APPLICATION_NOT_FOUND);
        }

        // 2️⃣ 요청한 모든 applicationId가 유효한지 확인 (소유권 또는 상태 문제)
        if (apps.size() != applicationIds.size()) {
            log.warn("일부 신청 취소 불가 - 요청: {}, 취소 가능: {} (소유권 또는 상태 문제)",
                    applicationIds.size(), apps.size());
            throw new CustomException(ErrorCode.UNAUTHORIZED_APPLICATION);
        }

        log.info("취소 가능한 신청 확인 완료 - {} 건", apps.size());

        // 3️⃣ session 단위 그룹핑 (대기자 승격용)
        Map<Long, List<Long>> sessionMap = new HashMap<>();

        for (TrainingCourseApplication app : apps) {
            sessionMap
                    .computeIfAbsent(app.getSessionId(), k -> new ArrayList<>())
                    .add(app.getApplicationId());
        }

        // 4️⃣ 세션별 취소 + 대기자 승격
        for (Map.Entry<Long, List<Long>> entry : sessionMap.entrySet()) {
            Long sessionId = entry.getKey();
            List<Long> appIds = entry.getValue();

            // 신청 취소
            applicationDao.updateApplicationStatusBatch(appIds, "CANCELLED");
            applicationDao.updateWaitingStatusBatch(appIds, "CANCELLED");

            // 대기자 승격 (새 로직: WAITING → ACCEPT)
            List<Long> waitingList = applicationDao.findWaitingBySessionId(sessionId);

            // 취소된 인원만큼 대기자 승격
            int promotionCount = Math.min(appIds.size(), waitingList.size());

            if (promotionCount > 0) {
                // 승격할 대기자 ID 목록
                List<Long> promotedApplicationIds = waitingList.subList(0, promotionCount);

                // 1. 신청 상태 일괄 변경: WAITING → ACCEPT
                for (Long nextApplicationId : promotedApplicationIds) {
                    trainerUserDao.updateApplicationStatusSimple(nextApplicationId, "ACCEPT");
                    trainerUserDao.updateWaitingStatus(nextApplicationId, "PROMOTED");
                }

                // 2. 출석 정보 일괄 생성 (필수)
                // 중요: 출석 정보 생성 실패 시 CustomException을 발생시켜
                //          전체 트랜잭션을 롤백합니다 (일괄 취소도 함께 취소됨).
                try {
                    int inserted = trainingAttendanceDao.insertAttendanceByApplicationIds(promotedApplicationIds, userId);

                    if (inserted != promotionCount) {
                        log.error("출석 정보 생성 실패 - 예상: {}, 실제: {}", promotionCount, inserted);
                        throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
                    }

                    log.info("일괄 취소 후 대기자 {}명 승격 완료 - applicationIds: {}", promotionCount, promotedApplicationIds);

                } catch (CustomException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("일괄 취소 후 대기자 승격 시 출석 정보 생성 실패", e);
                    throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
                }
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


