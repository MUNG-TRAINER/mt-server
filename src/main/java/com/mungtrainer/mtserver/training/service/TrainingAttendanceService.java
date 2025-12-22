package com.mungtrainer.mtserver.training.service;

     import com.mungtrainer.mtserver.common.exception.CustomException;
     import com.mungtrainer.mtserver.common.exception.ErrorCode;
     import com.mungtrainer.mtserver.common.s3.S3Service;
     import com.mungtrainer.mtserver.training.dto.request.AttendanceUpdateRequest;
     import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
     import com.mungtrainer.mtserver.training.dao.TrainingAttendanceDAO;
     import lombok.RequiredArgsConstructor;
     import lombok.extern.slf4j.Slf4j;
     import org.springframework.stereotype.Service;
     import org.springframework.transaction.annotation.Transactional;

     import java.util.List;
     import java.util.stream.Collectors;

     @Slf4j
     @Service
     @RequiredArgsConstructor
     @Transactional(readOnly = true)
     public class TrainingAttendanceService {

         private final TrainingAttendanceDAO trainingAttendanceMapper;
         private final S3Service s3Service;

         /**
          * 특정 세션의 모든 출석 목록 조회
          * 반려견 프로필 이미지를 Presigned URL로 변환하여 반환
          */
         public List<AttendanceListResponse> getAttendanceList(Long sessionId) {
             log.info("세션 ID: {}의 출석 목록 조회 요청", sessionId);

             List<AttendanceListResponse> attendanceList = trainingAttendanceMapper.findBySessionId(sessionId);

             // 반려견 프로필 이미지를 Presigned URL로 변환
             List<AttendanceListResponse> result = attendanceList.stream()
                     .map(this::convertProfileImageToPresignedUrl)
                     .collect(Collectors.toList());

             log.info("세션 ID: {}의 출석 목록 조회 완료 (총 {}건)", sessionId, result.size());
             return result;
         }

         /**
          * 반려견 프로필 이미지를 S3 Presigned URL로 변환
          */
         private AttendanceListResponse convertProfileImageToPresignedUrl(AttendanceListResponse response) {
             String profileImageKey = response.getDogProfileImage();

             // 프로필 이미지가 있는 경우 Presigned URL 생성
             if (profileImageKey != null && !profileImageKey.isBlank()) {
                 try {
                     String presignedUrl = s3Service.generateDownloadPresignedUrl(profileImageKey);
                     response.setDogProfileImage(presignedUrl);
                 } catch (Exception e) {
                     log.warn("프로필 이미지 Presigned URL 생성 실패 - key: {}, error: {}",
                             profileImageKey, e.getMessage());
                     // 실패해도 null로 설정하여 나머지 데이터는 정상 반환
                     response.setDogProfileImage(null);
                 }
             }

             return response;
         }

         /**
          * 특정 세션의 특정 회원 출석 상태 변경
          */
         @Transactional
         public void updateAttendanceStatus(Long sessionId, String userName, AttendanceUpdateRequest request) {
             log.info("세션 ID: {}, 회원: {}의 출석 상태 변경 요청 - 상태: {}", sessionId, userName, request.getStatus());

             // 출석 상태 업데이트
             int updatedCount = trainingAttendanceMapper.updateStatus(
                 sessionId,
                 userName,
                 request.getStatus(),
                 request.getMemo()
             );

             if (updatedCount == 0) {
                 log.error("세션 ID: {}, 회원: {}의 출석 상태 변경 실패", sessionId, userName);
               throw new CustomException(ErrorCode.ATTENDANCE_UPDATE_FAILED);
             }

             log.info("세션 ID: {}, 회원: {}의 출석 상태 변경 완료", sessionId, userName);
         }
     }