package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.dao.TrainingCourseDao;
import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchItemDto;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchResponse;
import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingCourseService {

    private final TrainingCourseDao trainingCourseDao;
    private final TrainerUserDAO trainerUserDAO;
    private final S3Service s3Service;

    public TrainingCourseResponse getCourseById(Long courseId){
        TrainingCourse trainingCourse = trainingCourseDao.findByCourseId(courseId);
        if(trainingCourse == null) {
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

       // DB에 저장된 파일 key
        String mainFileKey = trainingCourse.getMainImage();
        String detailFileKey = trainingCourse.getDetailImage();

       // presigned URL 생성
        String mainPresignedUrl = null;
        String detailPresignedUrl = null;

        if (mainFileKey != null && !mainFileKey.isBlank()) {
            mainPresignedUrl = s3Service.generateDownloadPresignedUrl(mainFileKey);
        }

        if (detailFileKey != null && !detailFileKey.isBlank()) {
            detailPresignedUrl = s3Service.generateDownloadPresignedUrl(detailFileKey);
        }

        return TrainingCourseResponse.builder()
                .trainerId(trainingCourse.getTrainerId())
                .tags(trainingCourse.getTags())
                .title(trainingCourse.getTitle())
                .description(trainingCourse.getDescription())
                .type(trainingCourse.getType())
                .lessonForm(trainingCourse.getLessonForm())
                .status(trainingCourse.getStatus())
                .isFree(trainingCourse.getIsFree())
                .difficulty(trainingCourse.getDifficulty())
                .location(trainingCourse.getLocation())
                .schedule(trainingCourse.getSchedule())
                .refundPolicy(trainingCourse.getRefundPolicy())
                .mainImageKey(trainingCourse.getMainImage())
                .mainImage(mainPresignedUrl)
                .detailImageKey(trainingCourse.getDetailImage())
                .detailImage(detailPresignedUrl)
                .items(trainingCourse.getItems())
                .dogSize(trainingCourse.getDogSize())
                .build();
    }

    /**
     * 훈련과정 검색 (무한 스크롤) - 역할 기반 필터링
     *
     * @param request 검색 요청 정보
     * @param userDetails 인증된 사용자 정보
     * @return 검색 결과
     */
    public CourseSearchResponse searchCourses(CourseSearchRequest request, CustomUserDetails userDetails) {
        // 역할에 따른 trainerId 설정
        Long trainerId = determineTrainerId(userDetails);

        // size + 1개 조회하여 다음 페이지 존재 여부 확인
        CourseSearchRequest searchRequest = CourseSearchRequest.builder()
                .keyword(request.getKeyword())
                .lastCourseId(request.getLastCourseId())
                .size(request.getSize() + 1)
                .trainerId(trainerId)
                .build();

        List<CourseSearchItemDto> courses = trainingCourseDao.searchCourses(searchRequest);

        // 다음 페이지 존재 여부 확인
        boolean hasMore = courses.size() > request.getSize();
        if (hasMore) {
            courses = courses.subList(0, request.getSize());
        }

        // S3 Presigned URL 생성
        List<CourseSearchItemDto> coursesWithPresignedUrl = courses.stream()
                .map(course -> {
                    String presignedUrl = course.getMainImage();
                    if (course.getMainImage() != null && !course.getMainImage().isBlank()) {
                        presignedUrl = s3Service.generateDownloadPresignedUrl(course.getMainImage());
                    }

                    return CourseSearchItemDto.builder()
                            .courseId(course.getCourseId())
                            .trainerId(course.getTrainerId())
                            .trainerName(course.getTrainerName())
                            .title(course.getTitle())
                            .description(course.getDescription())
                            .tags(course.getTags())
                            .mainImage(presignedUrl)
                            .type(course.getType())
                            .lessonForm(course.getLessonForm())
                            .status(course.getStatus())
                            .difficulty(course.getDifficulty())
                            .isFree(course.getIsFree())
                            .location(course.getLocation())
                            .schedule(course.getSchedule())
                            .dogSize(course.getDogSize())
                            .session(course.getSession())
                            .build();
                })
                .collect(Collectors.toList());

        // 마지막 courseId 추출
        Long lastCourseId = coursesWithPresignedUrl.isEmpty()
                ? null
                : coursesWithPresignedUrl.get(coursesWithPresignedUrl.size() - 1).getCourseId();

        return CourseSearchResponse.builder()
                .courses(coursesWithPresignedUrl)
                .hasMore(hasMore)
                .lastCourseId(lastCourseId)
                .size(coursesWithPresignedUrl.size())
                .build();
    }

    /**
     * 사용자 역할에 따른 trainerId 결정
     *
     * @param userDetails 인증된 사용자 정보
     * @return trainerId (일반 유저: 연결된 훈련사 ID, 훈련사: 본인 ID)
     */
    private Long determineTrainerId(CustomUserDetails userDetails) {
        String role = userDetails.getRole();
        Long userId = userDetails.getUserId();

        if ("TRAINER".equals(role)) {
            // 훈련사: 본인의 userId가 곧 trainerId
            return userId;
        } else if ("USER".equals(role)) {
            // 일반 유저: trainer_user 테이블에서 연결된 trainerId 조회
            Long trainerId = trainerUserDAO.findTrainerIdByUserId(userId);
            if (trainerId == null) {
                throw new CustomException(ErrorCode.TRAINER_NOT_FOUND);
            }
            return trainerId;
        }

        // 권한이 없는 경우
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
