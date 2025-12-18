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
     * 훈련과정 검색 (무한 스크롤)
     */
    public CourseSearchResponse searchCourses(CourseSearchRequest request) {
        // size + 1개 조회하여 다음 페이지 존재 여부 확인
        CourseSearchRequest searchRequest = CourseSearchRequest.builder()
                .keyword(request.getKeyword())
                .lastCourseId(request.getLastCourseId())
                .size(request.getSize() + 1)
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
}
