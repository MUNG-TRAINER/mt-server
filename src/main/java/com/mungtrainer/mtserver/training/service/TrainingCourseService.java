package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.auth.entity.CustomUserDetails;
import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.dao.TrainingCourseDao;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import com.mungtrainer.mtserver.training.dto.request.CourseSearchRequest;
import com.mungtrainer.mtserver.training.dto.response.CalendarResponse;
import com.mungtrainer.mtserver.training.dto.response.CalendarSessionDateDto;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchItemDto;
import com.mungtrainer.mtserver.training.dto.response.CourseSearchResponse;
import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TrainingCourseService {

    private final TrainingCourseDao trainingCourseDao;
    private final TrainerUserDAO trainerUserDAO;
    private final S3Service s3Service;
    private final TrainingSessionDAO trainingSessionDAO;

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
        List<String> detailPresignedUrls = List.of();

        if (detailFileKey != null && !detailFileKey.isBlank()) {
          detailPresignedUrls = Arrays.stream(detailFileKey.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .toList();
        }

        if (mainFileKey != null && !mainFileKey.isBlank()) {
            mainPresignedUrl = s3Service.generateDownloadPresignedUrl(mainFileKey);
        }

        List<String> afterDetailPresignedUrls = s3Service.generateDownloadPresignedUrls(detailPresignedUrls);


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
                .detailImageUrls(afterDetailPresignedUrls)
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
                .lessonForm(request.getLessonForm())
                .build();

        List<CourseSearchItemDto> courses = trainingCourseDao.searchCourses(searchRequest);

        // 다음 페이지 존재 여부 확인
        boolean hasMore = courses.size() > request.getSize();
        if (hasMore) {
            courses = courses.subList(0, request.getSize());
        }

      // S3 Presigned URL 생성 (기존 DTO를 재사용하면서 mainImage만 수정)
      for (CourseSearchItemDto course : courses) {
        String presignedUrl = course.getMainImage();
        if (course.getMainImage() != null && !course.getMainImage().isBlank()) {
          presignedUrl = s3Service.generateDownloadPresignedUrl(course.getMainImage());
        }
        course.setMainImage(presignedUrl);
      }
      List<CourseSearchItemDto> coursesWithPresignedUrl = courses;

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

    /**
     * 특정 기간의 세션 날짜 목록 조회 (달력용)
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param userDetails 인증된 사용자 정보
     * @param keyword 검색 키워드
     * @param lessonForm 수업 형태 필터
     * @return 달력 응답 (세션이 있는 날짜 목록)
     */
    public CalendarResponse getCalendarByPeriod(
            LocalDate startDate,
            LocalDate endDate,
            CustomUserDetails userDetails,
            String keyword,
            String lessonForm) {

        // 역할에 따른 trainerId 설정
        Long trainerId = determineTrainerId(userDetails);

        // 세션 날짜 목록 조회
        List<CalendarSessionDateDto> sessionDates = trainingSessionDAO.findSessionDatesByPeriod(
                startDate, endDate, trainerId, keyword, lessonForm);

        return CalendarResponse.builder()
                .sessionDates(sessionDates)
                .totalDates(sessionDates.size())
                .build();
    }

    /**
     * 특정 날짜의 코스 목록 조회 (CourseSearchResponse 형식)
     *
     * @param date 조회할 날짜
     * @param userDetails 인증된 사용자 정보
     * @param keyword 검색 키워드
     * @param lessonForm 수업 형태 필터
     * @return 해당 날짜의 코스 목록 (CourseSearchResponse 형식)
     */
    public CourseSearchResponse getCoursesByDate(
            LocalDate date,
            CustomUserDetails userDetails,
            String keyword,
            String lessonForm) {

        // 역할에 따른 trainerId 설정
        Long trainerId = determineTrainerId(userDetails);

        // 특정 날짜의 코스 목록 조회
        List<CourseSearchItemDto> courses = trainingCourseDao.findCoursesByDate(
                date, trainerId, keyword, lessonForm);

        // S3 Presigned URL 생성
        for (CourseSearchItemDto course : courses) {
          String mainImage = course.getMainImage();
          if (mainImage != null && !mainImage.isBlank()) {
            course.setMainImage(s3Service.generateDownloadPresignedUrl(mainImage));
          }
        }

        return CourseSearchResponse.builder()
                .courses(courses)
                .hasMore(false)  // 특정 날짜 조회는 페이지네이션 없음
                .lastCourseId(null)
                .size(courses.size())
                .build();
    }
}
