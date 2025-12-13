package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.s3.S3Service;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.training.dao.UserCourseDAO;
import com.mungtrainer.mtserver.training.dto.response.UserCourseGroupedResponse;
import com.mungtrainer.mtserver.training.dto.response.UserCourseResponse;
import com.mungtrainer.mtserver.training.dto.response.UserCourseSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCourseService {

    private final UserCourseDAO userCourseDAO;
    private final S3Service s3Service;

    public List<UserCourseGroupedResponse> getUserCourses(Long userId, String status) {

        List<UserCourseResponse> flatList =
                userCourseDAO.selectUserCourses(userId, status);

        if (flatList.isEmpty()) {
            return List.of();
        }

        // 1. mainImage S3 key 수집
        List<String> imageKeys = flatList.stream()
                .map(UserCourseResponse::getMainImage)
                .filter(Objects::nonNull)
                .distinct()
                .toList();


        // 2. Presigned URL 일괄 발급
        List<String> presignedUrls = s3Service.generateDownloadPresignedUrls(imageKeys);

        Map<String, String> imageUrlMap = new HashMap<>();
        for (int i = 0; i < imageKeys.size(); i++) {
            imageUrlMap.put(imageKeys.get(i), presignedUrls.get(i));
        }

        // 3. key ↔ url 매핑
        Map<Long, UserCourseGroupedResponse> courseMap = new LinkedHashMap<>();

        for (UserCourseResponse row : flatList) {

            // course 단위 생성
            UserCourseGroupedResponse course =
                    courseMap.computeIfAbsent(row.getCourseId(), id -> {
                        UserCourseGroupedResponse c = new UserCourseGroupedResponse();
                        c.setCourseId(row.getCourseId());
                        c.setTitle(row.getTitle());

//                        mainImage: S3 key → Presigned URL
                        String imageKey = row.getMainImage();
                        c.setMainImage(
                                imageKey == null ? null : imageUrlMap.get(imageKey)
                        );

                        c.setLessonForm(row.getLessonForm());
                        c.setDifficulty(row.getDifficulty());
                        c.setLocation(row.getLocation());
                        c.setType(row.getType());
                        c.setSessions(new ArrayList<>());
                        return c;
                    });

            // session 추가
            UserCourseSessionResponse session = new UserCourseSessionResponse();
            session.setSessionId(row.getSessionId());
            session.setSessionNo(row.getSessionNo());
            session.setSessionDate(row.getSessionDate());
            session.setStartTime(row.getStartTime());
            session.setEndTime(row.getEndTime());
            session.setSessionStatus(row.getSessionStatus());
            session.setApplicationStatus(row.getApplicationStatus());

            course.getSessions().add(session);
        }

        return new ArrayList<>(courseMap.values());
    }
}
