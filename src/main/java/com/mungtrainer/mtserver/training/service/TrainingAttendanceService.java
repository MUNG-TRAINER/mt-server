package com.mungtrainer.mtserver.training.service;

     import com.mungtrainer.mtserver.training.dto.request.AttendanceUpdateRequest;
     import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
     import com.mungtrainer.mtserver.training.entity.TrainingAttendance;
     import com.mungtrainer.mtserver.training.mapper.TrainingAttendanceMapper;
     import lombok.RequiredArgsConstructor;
     import lombok.extern.slf4j.Slf4j;
     import org.springframework.stereotype.Service;
     import org.springframework.transaction.annotation.Transactional;

     import java.util.List;

     @Slf4j
     @Service
     @RequiredArgsConstructor
     @Transactional(readOnly = true)
     public class TrainingAttendanceService {

         private final TrainingAttendanceMapper trainingAttendanceMapper;

         /**
          * 특정 세션의 모든 출석 목록 조회
          */
         public List<AttendanceListResponse> getAttendanceList(Long sessionId) {
             log.info("세션 ID: {}의 출석 목록 조회 요청", sessionId);

             List<AttendanceListResponse> attendanceList = trainingAttendanceMapper.findBySessionId(sessionId);

             log.info("세션 ID: {}의 출석 목록 조회 완료 (총 {}건)", sessionId, attendanceList.size());
             return attendanceList;
         }

         /**
          * 특정 세션의 특정 회원 출석 상태 변경
          */
         @Transactional
         public void updateAttendanceStatus(Long sessionId, String userName, AttendanceUpdateRequest request) {
             log.info("세션 ID: {}, 회원: {}의 출석 상태 변경 요청 - 상태: {}", sessionId, userName, request.getStatus());

             // 출석 정보 존재 여부 확인
             TrainingAttendance attendance = trainingAttendanceMapper.findBySessionIdAndUserName(sessionId, userName);
             if (attendance == null) {
                 log.error("세션 ID: {}, 회원: {}의 출석 정보를 찾을 수 없습니다", sessionId, userName);
                 throw new IllegalArgumentException("해당 회원의 출석 정보를 찾을 수 없습니다");
             }

             // 출석 상태 업데이트
             int updatedCount = trainingAttendanceMapper.updateStatus(
                 sessionId,
                 userName,
                 request.getStatus(),
                 request.getMemo()
             );

             if (updatedCount == 0) {
                 log.error("세션 ID: {}, 회원: {}의 출석 상태 변경 실패", sessionId, userName);
                 throw new IllegalStateException("출석 상태 변경에 실패했습니다");
             }

             log.info("세션 ID: {}, 회원: {}의 출석 상태 변경 완료", sessionId, userName);
         }
     }