package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
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
     * 훈련과정 검색
     */
    public CourseSearchResponse searchCourses(CourseSearchRequest request) {
        // 검색 실행
        List<CourseSearchItemDto> courses = trainingCourseDao.searchCourses(request);

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
                            .lessonForm(course.getLessonForm())
                            .difficulty(course.getDifficulty())
                            .location(course.getLocation())
                            .type(course.getType())
                            .price(course.getPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // 전체 개수 조회
        Integer totalCount = trainingCourseDao.countSearchResults(request);

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

        return CourseSearchResponse.builder()
                .courses(coursesWithPresignedUrl)
                .totalCount(totalCount)
                .currentPage(request.getPage())
                .totalPages(totalPages)
                .pageSize(request.getSize())
                .build();
    }
}
