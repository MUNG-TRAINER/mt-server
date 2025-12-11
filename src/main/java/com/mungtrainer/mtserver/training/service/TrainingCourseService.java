package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.config.S3Service;
import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.trainer.dto.response.TrainerResponse;
import com.mungtrainer.mtserver.training.dao.TrainingCourseDao;
import com.mungtrainer.mtserver.training.dto.response.TrainingCourseResponse;
import com.mungtrainer.mtserver.training.entity.TrainingCourse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                .mainImage(mainPresignedUrl)
                .detailImage(detailPresignedUrl)
                .items(trainingCourse.getItems())
                .dogSize(trainingCourse.getDogSize())
                .build();
    }
}

