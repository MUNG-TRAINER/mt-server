package com.mungtrainer.mtserver.counseling.service;

import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.counseling.dao.CounselingDao;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDao;
import com.mungtrainer.mtserver.counseling.dto.request.ApplicationStatusUpdateRequestDTO;
import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.mapper.DogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainerUserService {

    private final DogMapper dogMapper;
    private final TrainerUserDao trainerUserDao;
    private final S3Service s3Service;
    private final CounselingDao counselingDao;

    public List<TrainerUserListResponseDTO> getUsersByTrainer(Long trainerId) {
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
    public DogStatsResponseDTO getDogStats(Long dogId, Long trainerId) {

        // 1. 반려견 조회 + Presigned URL 변환
        DogResponse dog = dogMapper.selectDogById(dogId);
        if (dog == null) {
            throw new RuntimeException("Dog not found");
        }
        if (dog.getProfileImage() != null && !dog.getProfileImage().isBlank()) {
            String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
            dog.setProfileImage(presignedUrl);
        }

        // 2. 해당 훈련사가 작성한 상담 내역 조회
        List<CounselingResponseDTO> counselings =
                counselingDao.selectCounselingsByDogAndTrainer(dogId);

        // 2. 단회차(일회차) 신청 내역 조회
        List<TrainingApplicationResponseDTO> list =
                trainerUserDao.findTrainingApplicationsByDogId(dogId);

        // timesApplied / attendedCount는 첫 row에서만 가져오기
        Integer timesApplied = list.isEmpty() ? 0 : list.get(0).getTimesApplied();
        Integer attendedCount = list.isEmpty() ? 0 : list.get(0).getAttendedCount();

        // 응답용 세션 리스트 변환 — 통계 정보 제거
        List<DogStatsResponseDTO.TrainingSessionDto> simplified =
                list.stream()
                        .map(item -> DogStatsResponseDTO.TrainingSessionDto.builder()
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

        // 3. 다회차 과정 목록 조회
        List<MultiCourseGroupDTO> rawMultiCourses = trainerUserDao.findMultiCoursesByDogId(dogId);


        // 3-1. 세션 + 출석률 계산
        for (MultiCourseGroupDTO mc : rawMultiCourses) {

            int totalSessions = trainerUserDao.countSessionsByCourseId(mc.getCourseId());
            mc.setTotalSessions(totalSessions);

            List<MultiSessionDTO> sessions =
                    trainerUserDao.findSessionsWithAttendance(dogId, mc.getCourseId());
            mc.setSessions(sessions);

            int attendedSessions = trainerUserDao.countAttendedSessions(dogId, mc.getCourseId());
            mc.setAttendedSessions(attendedSessions);

            double rate = totalSessions == 0 ? 0 : (attendedSessions * 100.0 / totalSessions);
            mc.setAttendanceRate(rate);
        }

        // 3-2. tags 기준으로 그룹화
        Map<String, List<MultiCourseGroupDTO>> grouped =
                rawMultiCourses.stream()
                        .collect(Collectors.groupingBy(MultiCourseGroupDTO::getTags));

        // 3-3. 응답용 구조로 변환
        List<MultiCourseCategoryDTO> finalGroups = grouped.entrySet().stream()
                .map(e -> new MultiCourseCategoryDTO(e.getKey(), e.getValue()))
                .toList();

        return DogStatsResponseDTO.builder()
                .dog(dog)
                .counselings(counselings)
                .stats(new DogStatsResponseDTO.Stats(timesApplied, attendedCount))
                .trainingApplications(simplified)
                .multiCourses(finalGroups)
                .build();

    }

    public List<appliedWatingDTO> getWatingApplications() {
        return trainerUserDao.selectWaitingApplications();
    }


    public void updateApplicationStatus(Long applicationId,
                                        ApplicationStatusUpdateRequestDTO req,
                                        Long trainerId) {

        String status = req.getStatus();

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
                throw new IllegalArgumentException("잘못된 status 값입니다.");
        }

        if (updated == 0) {
            throw new IllegalStateException("APPLIED 상태일 때만 승인/거절이 가능합니다.");
        }
    }

}
