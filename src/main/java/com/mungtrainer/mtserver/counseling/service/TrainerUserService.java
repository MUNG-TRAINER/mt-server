package com.mungtrainer.mtserver.counseling.service;


import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.counseling.dao.CounselingDAO;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.counseling.dto.request.ApplicationStatusUpdateRequest;
import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.dao.DogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerUserService {

    private final DogMapper dogMapper;
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
        List<DogResponse> dogs = dogMapper.selectDogsByUserId(userId);

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
        DogResponse dog = dogMapper.selectDogById(dogId);
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


    public List<AppliedWaitingResponse> getWaitingApplications() {
        return trainerUserDao.selectWaitingApplications();
    }


    public void updateApplicationStatus(Long applicationId,
                                        ApplicationStatusUpdateRequest req,
                                        Long trainerId) {

        // ===== 1. 기본 검증 =====
        if (req == null) {
            throw new RuntimeException("요청 데이터가 존재하지 않습니다.");
        }

        String status = req.getStatus();
        if (status == null || status.isBlank()) {
            throw new RuntimeException("status 값은 필수입니다.");
        }

        // ===== 2. 상태별 추가 검증 =====
        // 승인/거절 외 다른 값이 들어올 때 차단
        if (!status.equals("ACCEPT") && !status.equals("REJECTED")) {
            throw new RuntimeException("잘못된 status 값입니다. (ACCEPT 또는 REJECTED)");
        }

        // 거절일 경우 rejectReason 필수
        if (status.equals("REJECTED")) {
            if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
                throw new RuntimeException("거절 사유(rejectReason)는 필수입니다.");
            }
        }

        // ===== 3. DB 반영 =====
        int updated;
        switch (status) {
            case "ACCEPT":
                updated = trainerUserDao.updateStatusApproved(applicationId, trainerId);
                break;

            case "REJECTED":
                updated = trainerUserDao.updateStatusRejected(
                        applicationId,
                        trainerId,
                        req.getRejectReason()
                );
                break;

            default:
                throw new IllegalStateException("예상치 못한 상태 값입니다.");  // 이 경우는 절대 올 수 없음
        }

        // ===== 4. DB 반영 결과 검증 =====
        if (updated == 0) {
            throw new IllegalStateException("APPLIED 상태일 때만 승인/거절이 가능합니다.");
        }
    }


}
