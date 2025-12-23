package com.mungtrainer.mtserver.counseling.service;


import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.CounselingDAO;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.counseling.dto.request.ApplicationStatusUpdateRequest;
import com.mungtrainer.mtserver.counseling.dto.request.BulkApplicationStatusRequest;
import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.dao.DogDAO;
import com.mungtrainer.mtserver.training.dao.TrainingAttendanceDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerUserService {

    private final DogDAO dogDao;
    private final TrainerUserDAO trainerUserDao;
    private final S3Service s3Service;
    private final CounselingDAO counselingDao;
    private final TrainingAttendanceDAO trainingAttendanceDao;

    public List<TrainerUserListResponse> getUsersByTrainer(Long trainerId) {
        // 1. DB에서 회원 리스트 조회
        List<TrainerUserListResponse> users = trainerUserDao.findUsersByTrainerId(trainerId);

        if (users.isEmpty()) return List.of();

        // 2. 프로필 이미지를 S3 Presigned URL로 변환
        users.forEach(user -> {
            if (user.getProfileImage() != null && !user.getProfileImage().isBlank()) {
                String presignedUrl = s3Service.generateDownloadPresignedUrl(user.getProfileImage());
                user.setProfileImage(presignedUrl);
            }
        });

        return users;
    }

    // 반려견 목록 조회
    public List<DogResponse> getDogsByUser(Long userId) {
// 훈련사가 해당 회원을 관리하는지 확인
//        if (!isUserManagedByTrainer(trainerId, userId)) {
//            throw new UnauthorizedException("해당 회원의 정보에 접근할 권한이 없습니다.");
//        }
        // 1. DB에서 반려견 리스트 조회
        List<DogResponse> dogs = dogDao.selectDogsByUserId(userId);

        if (dogs.isEmpty()) return List.of();

        // 2. 모든 반려견의 S3 키 추출
        List<String> imageKeys = dogs.stream()
                .map(DogResponse::getProfileImage)
                .collect(Collectors.toList());

        // 3. S3 Presigned URL 일괄 발급
        List<String> presignedUrls = s3Service.generateDownloadPresignedUrls(imageKeys);

        // 4. 각 반려견 객체에 URL 매핑
        for (int i = 0; i < dogs.size(); i++) {
            dogs.get(i).setProfileImage(presignedUrls.get(i));
        }

        return dogs;
    }

    @Transactional(readOnly = true)
    public DogStatsResponse getDogStats(Long dogId, Long trainerId) {

        // 1. 반려견 조회 + Presigned URL 변환
        DogResponse dog = dogDao.selectDogById(dogId);
        if (dog == null) {
            throw new RuntimeException("Dog not found");
        }
        if (dog.getProfileImage() != null && !dog.getProfileImage().isBlank()) {
            String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
            dog.setProfileImage(presignedUrl);
        }

        // 2. 상담 기록
        List<CounselingResponse> counselings =
                counselingDao.selectCounselingsByDogAndTrainer(dogId);

        // 3. 단회차 신청 내역 조회
        List<TrainingApplicationResponse> singleApps =
                trainerUserDao.findTrainingApplicationsByDogId(dogId);

        // 디버깅: 조회된 데이터 확인
        log.info("🔍 [DogStats] dogId={}, 단회차 신청 건수={}", dogId, singleApps.size());

        // 통계 계산 - 태그별로 다른 값이 나올 수 있으므로 중복 제거 후 합산
        int timesApplied = 0;
        int attendedCount = 0;

        if (!singleApps.isEmpty()) {
            // 태그별로 그룹화하여 중복 제거
            Map<String, TrainingApplicationResponse> tagStats = singleApps.stream()
                    .collect(Collectors.toMap(
                            TrainingApplicationResponse::getTags,
                            app -> app,
                            (existing, replacement) -> existing  // 중복 시 첫 번째 값 유지
                    ));

            // 모든 태그의 통계 합산
            for (TrainingApplicationResponse app : tagStats.values()) {
                Integer applied = app.getTimesApplied();
                Integer attended = app.getAttendedCount();

                timesApplied += (applied != null ? applied : 0);
                attendedCount += (attended != null ? attended : 0);
            }

            log.info("📊 [DogStats] 전체 통계 - timesApplied={}, attendedCount={}, 태그 수={}",
                    timesApplied, attendedCount, tagStats.size());
        } else {
            log.info("ℹ️ [DogStats] 단회차 신청 내역이 없습니다.");
        }

        List<DogStatsResponse.TrainingSessionDto> simplified =
                singleApps.stream()
                        .map(item -> DogStatsResponse.TrainingSessionDto.builder()
                                .courseId(item.getCourseId())
                                .courseTitle(item.getCourseTitle())
                                .courseDescription(item.getCourseDescription())
                                .tags(item.getTags())
                                .type(item.getType())
                                .difficulty(item.getDifficulty())  // 난이도 매핑
                                .sessionId(item.getSessionId())
                                .sessionDate(item.getSessionDate())
                                .sessionStartTime(item.getSessionStartTime())
                                .sessionEndTime(item.getSessionEndTime())
                                .attendanceStatus(item.getAttendanceStatus())  // 출석 상태 매핑
                                .build()
                        ).toList();

        // 4. 다회차 — 단일 SQL 조회
        List<MultiCourseGroupResponse> flatRows =
                trainerUserDao.findMultiCourseDetail(Map.of(
                        "dogId", dogId,
                        "trainerId", trainerId
                ));

        // 4-1. courseId로 먼저 그룹핑 (세션 병합)
        Map<Long, MultiCourseGroupResponse> groupedByCourseId = new HashMap<>();

        for (MultiCourseGroupResponse row : flatRows) {

            Long courseId = row.getCourseId();

            MultiCourseGroupResponse group = groupedByCourseId.get(courseId);

            // 그룹 신규 생성
            if (group == null) {
                group = MultiCourseGroupResponse.builder()
                        .courseId(row.getCourseId())
                        .title(row.getTitle())
                        .tags(row.getTags())
                        .description(row.getDescription())
                        .location(row.getLocation())
                        .type(row.getType())
                        .difficulty(row.getDifficulty())
                        .mainImage(row.getMainImage())
                        .totalSessions(row.getTotalSessions())
                        .attendedSessions(row.getAttendedSessions())
                        .attendanceRate(0) // 계산은 아래에서
                        .sessions(new ArrayList<>())
                        .build();

                // 출석률 계산
                int total = (row.getTotalSessions() == null) ? 0 : row.getTotalSessions();
                int attended = row.getAttendedSessions();
                double rate = total == 0 ? 0 : attended * 100.0 / total;
                group.setAttendanceRate(rate);

                groupedByCourseId.put(courseId, group);
            }

            // 세션 추가
            if (row.getSessions() != null && !row.getSessions().isEmpty()) {
                group.getSessions().addAll(row.getSessions());
            }
        }

        List<MultiCourseGroupResponse> courseList = new ArrayList<>(groupedByCourseId.values());

        // ⭐ 4-2. tags(UUID)로 재그룹화 - 같은 과정을 여러 번 수강한 경우 묶기
        Map<String, List<MultiCourseGroupResponse>> groupedByUuid = new HashMap<>();

        for (MultiCourseGroupResponse course : courseList) {
            String uuid = course.getTags();
            groupedByUuid.computeIfAbsent(uuid, k -> new ArrayList<>()).add(course);
        }

        // ⭐ 4-3. UUID별로 병합된 응답 생성
        List<MultiCourseGroupResponse> mergedCourses = new ArrayList<>();

        for (Map.Entry<String, List<MultiCourseGroupResponse>> entry : groupedByUuid.entrySet()) {
            List<MultiCourseGroupResponse> sameCourses = entry.getValue();

            // 단일 수강인 경우 그대로 사용
            if (sameCourses.size() == 1) {
                MultiCourseGroupResponse single = sameCourses.get(0);
                single.setEnrollmentCount(1);
                single.setEnrollmentHistory(null);
                mergedCourses.add(single);
                continue;
            }

            // 여러 번 수강한 경우 - 날짜순 정렬
            sameCourses.sort((a, b) -> {
                LocalDate aDate = a.getSessions().isEmpty() ? LocalDate.MIN
                    : a.getSessions().get(0).getSessionDate();
                LocalDate bDate = b.getSessions().isEmpty() ? LocalDate.MIN
                    : b.getSessions().get(0).getSessionDate();
                return aDate.compareTo(bDate);
            });

            // 수강 이력 생성
            List<MultiCourseGroupResponse.EnrollmentHistory> histories = new ArrayList<>();
            int totalSessionsSum = 0;
            int attendedSessionsSum = 0;

            for (int i = 0; i < sameCourses.size(); i++) {
                MultiCourseGroupResponse course = sameCourses.get(i);

                // 시작/종료일 계산
                List<LocalDate> dates = course.getSessions().stream()
                    .map(MultiSessionResponse::getSessionDate)
                    .sorted()
                    .toList();
                LocalDate startDate = dates.isEmpty() ? null : dates.get(0);
                LocalDate endDate = dates.isEmpty() ? null : dates.get(dates.size() - 1);

                // 수강 이력 추가
                histories.add(MultiCourseGroupResponse.EnrollmentHistory.builder()
                    .enrollmentNumber(i + 1)
                    .courseId(course.getCourseId())
                    .title(course.getTitle())
                    .description(course.getDescription())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalSessions(course.getTotalSessions())
                    .attendedSessions(course.getAttendedSessions())
                    .attendanceRate(course.getAttendanceRate())
                    .sessions(course.getSessions())
                    .build());

                // 전체 통계 합산
                totalSessionsSum += (course.getTotalSessions() != null ? course.getTotalSessions() : 0);
                attendedSessionsSum += course.getAttendedSessions();
            }

            // 대표 정보 (첫 번째 수강 기준)
            MultiCourseGroupResponse representative = sameCourses.get(0);

            // 전체 평균 출석률
            double overallRate = totalSessionsSum > 0
                ? (attendedSessionsSum * 100.0 / totalSessionsSum)
                : 0.0;

            // 병합된 응답 생성
            MultiCourseGroupResponse merged = MultiCourseGroupResponse.builder()
                .courseId(representative.getCourseId())
                .title(representative.getTitle())
                .tags(representative.getTags())
                .description(representative.getDescription())
                .location(representative.getLocation())
                .type(representative.getType())
                .difficulty(representative.getDifficulty())
                .mainImage(representative.getMainImage())
                .enrollmentCount(sameCourses.size())
                .enrollmentHistory(histories)
                .totalSessions(totalSessionsSum)
                .attendedSessions(attendedSessionsSum)
                .attendanceRate(overallRate)
                .sessions(new ArrayList<>())
                .build();

            mergedCourses.add(merged);
        }

        // 5. 태그별 그룹핑
        Map<String, List<MultiCourseGroupResponse>> groupedByTag =
                mergedCourses.stream()
                        .collect(Collectors.groupingBy(MultiCourseGroupResponse::getTags));

        List<MultiCourseCategoryResponse> finalGroups =
                groupedByTag.entrySet().stream()
                        .map(e -> new MultiCourseCategoryResponse(e.getKey(), e.getValue()))
                        .toList();

        // 6. 다회차 통계를 전체 통계에 합산
        for (MultiCourseGroupResponse course : mergedCourses) {
            timesApplied += (course.getTotalSessions() != null ? course.getTotalSessions() : 0);
            attendedCount += course.getAttendedSessions();
        }

        log.info("📊 [DogStats] 최종 통계 (단회차+다회차) - timesApplied={}, attendedCount={}",
                timesApplied, attendedCount);

        // 최종 응답
        return DogStatsResponse.builder()
                .dog(dog)
                .counselings(counselings)
                .stats(new DogStatsResponse.Stats(timesApplied, attendedCount))
                .trainingApplications(simplified)
                .multiCourses(finalGroups)
                .build();
    }


  /**
   * 특정 훈련사의 승인 대기 신청 목록을 조회합니다.
   *
   * <p>trainerId를 기준으로 해당 훈련사에게 들어온 신청 중,
   * 아직 승인 또는 거절 처리되지 않은 신청만 조회합니다.
   *
   * @param trainerId 승인 대기 신청을 조회할 훈련사의 식별자. 이 ID에 해당하는 훈련사의 신청만 조회됩니다.
   * @return 승인 대기 상태의 신청 목록
   */
    public List<AppliedWaitingResponse> getWaitingApplications(Long trainerId) {
        return trainerUserDao.selectWaitingApplications(trainerId);
    }

    /**
     * 코스별로 그룹핑된 승인 대기 목록 조회
     * 다회차 훈련의 경우 일괄 승인/거절할 수 있도록 개선
     */
    @Transactional(readOnly = true)
    public List<GroupedApplicationResponse> getGroupedWaitingApplications(Long trainerId) {
        return trainerUserDao.selectGroupedWaitingApplications(trainerId);
    }

    /**
     * 신청 반려견 상세 정보 조회
     * 훈련사가 승인 대기 목록에서 상세 모달을 볼 때 사용
     */
    @Transactional(readOnly = true)
    public ApplicationDogDetailResponse getApplicationDogDetail(Long applicationId, Long trainerId) {
        // 1. 신청 반려견 정보 조회
        ApplicationDogDetailResponse detail = trainerUserDao.selectApplicationDogDetail(applicationId, trainerId);

        if (detail == null) {
            throw new CustomException(ErrorCode.APPLICATION_DETAIL_NOT_FOUND);
        }

        // 2. 프로필 이미지 Presigned URL 발급
        if (detail.getProfileImageUrl() != null && !detail.getProfileImageUrl().isBlank()) {
            String presignedUrl = s3Service.generateDownloadPresignedUrl(detail.getProfileImageUrl());
            detail.setProfileImageUrl(presignedUrl);
        }

        return detail;
    }

  @Transactional
  public void updateApplicationStatus(Long applicationId,
                                        ApplicationStatusUpdateRequest req,
                                        Long trainerId) {

        // 기본 검증
        if (req == null) {
            throw new CustomException(ErrorCode.APPLICATION_STATUS_REQUEST_EMPTY);
        }

        // 상태 및 거절 사유 검증
        String status = validateApplicationStatusRequest(req.getStatus(), req.getRejectReason());

        // DB 반영
        int updated = executeStatusUpdate(status, applicationId, null, null, trainerId, req.getRejectReason());

        // DB 반영 결과 검증
        if (updated == 0) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        // 승인 시 출석 정보 생성
        if ("ACCEPT".equals(status)) {
            createAttendanceRecord(applicationId, trainerId);
        }
    }

    /**
     * 코스별 일괄 승인/거절 처리
     * 다회차 훈련의 모든 회차를 한 번에 승인/거절
     */
    @Transactional
    public void updateBulkApplicationStatus(Long courseId,
                                           Long dogId,
                                           BulkApplicationStatusRequest req,
                                           Long trainerId) {
        // 기본 검증
        if (req == null) {
            throw new CustomException(ErrorCode.APPLICATION_STATUS_REQUEST_EMPTY);
        }

        // 상태 및 거절 사유 검증
        String status = validateApplicationStatusRequest(req.getStatus(), req.getRejectReason());

        // DB 일괄 반영
        int updated = executeStatusUpdate(status, null, courseId, dogId, trainerId, req.getRejectReason());

        // DB 반영 결과 검증
        if (updated == 0) {
            throw new CustomException(ErrorCode.APPLICATION_NO_MATCHING_RECORD);
        }

        // 승인 시 해당 코스의 모든 신청에 대해 출석 정보 일괄 생성
        if ("ACCEPT".equals(status)) {
            createBulkAttendanceRecords(courseId, dogId, trainerId);
        }
    }

    /**
     * 신청 승인/거절 요청의 status와 거절 사유를 검증합니다.
     *
     * @param status 승인/거절 상태 (ACCEPT, REJECTED)
     * @param rejectReason 거절 사유
     * @return 검증된 status 값
     * @throws CustomException 검증 실패 시
     */
    private String validateApplicationStatusRequest(String status, String rejectReason) {
        // status 필수 검증
        if (status == null || status.isBlank()) {
            throw new CustomException(ErrorCode.APPLICATION_STATUS_REQUIRED);
        }

        // status 값 검증 (ACCEPT 또는 REJECTED만 허용)
        if (!status.equals("ACCEPT") && !status.equals("REJECTED")) {
            throw new CustomException(ErrorCode.APPLICATION_STATUS_INVALID);
        }

        // 거절 시 거절 사유 필수 검증
        if (status.equals("REJECTED")) {
            if (rejectReason == null || rejectReason.isBlank()) {
                throw new CustomException(ErrorCode.APPLICATION_REJECT_REASON_REQUIRED);
            }
        }

        return status;
    }

    /**
     * 승인/거절 상태 업데이트를 실행합니다.
     * 개별 승인/거절과 일괄 승인/거절 모두에서 사용됩니다.
     *
     * @param status 승인/거절 상태
     * @param applicationId 개별 신청 ID (개별 처리 시)
     * @param courseId 코스 ID (일괄 처리 시)
     * @param dogId 반려견 ID (일괄 처리 시)
     * @param trainerId 훈련사 ID
     * @param rejectReason 거절 사유
     * @return 업데이트된 행 수
     */
    private int executeStatusUpdate(String status, Long applicationId, Long courseId, Long dogId,
                                    Long trainerId, String rejectReason) {
        int updated;

        if ("ACCEPT".equals(status)) {
            // 일괄 승인
            if (courseId != null && dogId != null) {
                updated = trainerUserDao.updateBulkStatusApproved(courseId, dogId, trainerId);
            }
            // 개별 승인
            else {
                updated = trainerUserDao.updateStatusApproved(applicationId, trainerId);
            }
        }
        else if ("REJECTED".equals(status)) {
            // 일괄 거절
            if (courseId != null && dogId != null) {
                updated = trainerUserDao.updateBulkStatusRejected(courseId, dogId, trainerId, rejectReason);
            }
            // 개별 거절
            else {
                updated = trainerUserDao.updateStatusRejected(applicationId, trainerId, rejectReason);
            }
        }
        else {
            throw new CustomException(ErrorCode.APPLICATION_STATUS_INVALID);
        }

        return updated;
    }

    /**
     * 개별 신청에 대한 출석 정보 생성
     * 신청이 승인되었을 때 호출됩니다.
     * 출석 정보 생성 실패 시 트랜잭션을 롤백하여 데이터 일관성을 보장합니다.
     *
     * @param applicationId 승인된 신청 ID
     * @param trainerId 승인한 훈련사 ID (감사 추적용)
     * @throws CustomException 출석 정보 생성 실패 시
     */
    private void createAttendanceRecord(Long applicationId, Long trainerId) {
        log.info("출석 정보 생성 시작 - 신청 ID: {}, 생성자: {}", applicationId, trainerId);

        int inserted = trainingAttendanceDao.insertAttendanceByApplicationId(applicationId, trainerId);
        validateAttendanceCreation(inserted, 1, "신청 ID: " + applicationId);

        log.info("출석 정보 생성 완료 - 신청 ID: {}, 생성된 레코드 수: {}", applicationId, inserted);
    }

    /**
     * 여러 신청에 대한 출석 정보 일괄 생성
     * 다회차 코스 일괄 승인 시 호출됩니다.
     * 출석 정보 생성 실패 시 트랜잭션을 롤백하여 데이터 일관성을 보장합니다.
     *
     * @param courseId 코스 ID
     * @param dogId 반려견 ID
     * @param trainerId 승인한 훈련사 ID (감사 추적용)
     * @throws CustomException 출석 정보 생성 실패 시
     */
    private void createBulkAttendanceRecords(Long courseId, Long dogId, Long trainerId) {
        log.info("일괄 출석 정보 생성 시작 - 코스 ID: {}, 반려견 ID: {}, 생성자: {}", courseId, dogId, trainerId);

        // 해당 코스와 반려견의 모든 승인된 신청 ID 조회
        List<Long> applicationIds = trainerUserDao.findApplicationIdsByCourseAndDog(courseId, dogId);

        if (applicationIds == null || applicationIds.isEmpty()) {
            log.error("출석 정보 생성 대상 없음 - 코스 ID: {}, 반려견 ID: {}", courseId, dogId);
            throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
        }

        // 출석 정보 일괄 생성
        int inserted = trainingAttendanceDao.insertAttendanceByApplicationIds(applicationIds, trainerId);
        validateAttendanceCreation(inserted, applicationIds.size(),
                String.format("코스 ID: %d, 반려견 ID: %d", courseId, dogId));

        log.info("일괄 출석 정보 생성 완료 - 코스 ID: {}, 반려견 ID: {}, 생성된 레코드 수: {}",
                courseId, dogId, inserted);
    }

    /**
     * 출석 정보 생성 결과를 검증합니다.
     * 생성된 레코드 수가 예상과 일치하지 않으면 예외를 발생시킵니다.
     *
     * @param actualCount 실제 생성된 레코드 수
     * @param expectedCount 예상 생성 레코드 수
     * @param context 에러 로그에 포함될 컨텍스트 정보
     * @throws CustomException 검증 실패 시
     */
    private void validateAttendanceCreation(int actualCount, int expectedCount, String context) {
        if (actualCount == 0) {
            log.error("출석 정보 생성 실패 - {}, 삽입된 레코드 수: 0", context);
            throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
        }

        if (actualCount != expectedCount) {
            log.error("출석 정보 생성 불완전 - {}, 예상: {}, 실제: {}",
                    context, expectedCount, actualCount);
            throw new CustomException(ErrorCode.ATTENDANCE_CREATION_FAILED);
        }
    }


}
