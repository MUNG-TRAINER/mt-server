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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerUserService {

    private final DogDAO dogDao;
    private final TrainerUserDAO trainerUserDao;
    private final S3Service s3Service;
    private final CounselingDAO counselingDao;

    public List<TrainerUserListResponse> getUsersByTrainer(Long trainerId) {
        return trainerUserDao.findUsersByTrainerId(trainerId);
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

        Integer timesApplied = singleApps.isEmpty() ? 0 : singleApps.get(0).getTimesApplied();
        Integer attendedCount = singleApps.isEmpty() ? 0 : singleApps.get(0).getAttendedCount();

        List<DogStatsResponse.TrainingSessionDto> simplified =
                singleApps.stream()
                        .map(item -> DogStatsResponse.TrainingSessionDto.builder()
                                .courseId(item.getCourseId())
                                .courseTitle(item.getCourseTitle())
                                .courseDescription(item.getCourseDescription())
                                .tags(item.getTags())
                                .type(item.getType())
                                .sessionId(item.getSessionId())
                                .sessionDate(item.getSessionDate())
                                .sessionStartTime(item.getSessionStartTime())
                                .sessionEndTime(item.getSessionEndTime())
                                .build()
                        ).toList();

        // 4. 다회차 — 단일 SQL 조회
        List<MultiCourseGroupResponse> flatRows =
                trainerUserDao.findMultiCourseDetail(Map.of(
                        "dogId", dogId,
                        "trainerId", trainerId
                ));

        // 4-1. 그룹핑
        Map<Long, MultiCourseGroupResponse> grouped = new HashMap<>();

        for (MultiCourseGroupResponse row : flatRows) {

            Long courseId = row.getCourseId();

            MultiCourseGroupResponse group = grouped.get(courseId);

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

                grouped.put(courseId, group);
            }

            // 세션 추가
            if (row.getSessions() != null && !row.getSessions().isEmpty()) {
                group.getSessions().addAll(row.getSessions());
            }
        }

        // 4-2. 그룹 리스트 변환
        List<MultiCourseGroupResponse> multiCourses =
                new ArrayList<>(grouped.values());

        // 5. 태그별 그룹핑
        Map<String, List<MultiCourseGroupResponse>> groupedByTag =
                multiCourses.stream()
                        .collect(Collectors.groupingBy(MultiCourseGroupResponse::getTags));

        List<MultiCourseCategoryResponse> finalGroups =
                groupedByTag.entrySet().stream()
                        .map(e -> new MultiCourseCategoryResponse(e.getKey(), e.getValue()))
                        .toList();

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


}
