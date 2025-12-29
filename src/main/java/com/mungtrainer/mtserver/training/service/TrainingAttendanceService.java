package com.mungtrainer.mtserver.training.service;

     import com.mungtrainer.mtserver.common.exception.CustomException;
     import com.mungtrainer.mtserver.common.exception.ErrorCode;
     import com.mungtrainer.mtserver.common.s3.S3Service;
     import com.mungtrainer.mtserver.training.dto.request.AttendanceUpdateRequest;
     import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
     import com.mungtrainer.mtserver.training.dao.TrainingAttendanceDAO;
     import lombok.RequiredArgsConstructor;
     import org.springframework.stereotype.Service;
     import org.springframework.transaction.annotation.Transactional;

     import java.util.List;
     import java.util.stream.Collectors;

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
             List<AttendanceListResponse> attendanceList = trainingAttendanceMapper.findBySessionId(sessionId);

             // 반려견 프로필 이미지를 Presigned URL로 변환
             return attendanceList.stream()
                     .map(this::convertProfileImageToPresignedUrl)
                     .collect(Collectors.toList());
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
                     // 실패해도 null로 설정하여 나머지 데이터는 정상 반환
                     response.setDogProfileImage(null);
                 }
             }

             return response;
         }

         /**
          * 특정 반려견의 출석 상태 변경
          */
         @Transactional
         public void updateAttendanceStatus(Long attendanceId, AttendanceUpdateRequest request) {

             // 출석 상태 업데이트 (attendanceId로 직접 업데이트)
             int updatedCount = trainingAttendanceMapper.updateStatusByAttendanceId(
                 attendanceId,
                 request.getStatus(),
                 request.getMemo()
             );

             if (updatedCount == 0) {
               throw new CustomException(ErrorCode.ATTENDANCE_UPDATE_FAILED);
             }
         }
     }