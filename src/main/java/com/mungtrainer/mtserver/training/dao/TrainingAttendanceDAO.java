package com.mungtrainer.mtserver.training.dao;

import com.mungtrainer.mtserver.training.dto.response.AttendanceListResponse;
import com.mungtrainer.mtserver.training.entity.TrainingAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingAttendanceDAO {

  /**
   * 특정 세션의 모든 출석 목록 조회 (DTO 반환)
   */
  List<AttendanceListResponse> findBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 특정 세션에서 특정 사용자(userName)의 출석 정보 조회
   */
  TrainingAttendance findBySessionIdAndUserName(
      @Param("sessionId") Long sessionId,
      @Param("userName") String userName
  );

  /**
   * 출석 상태 업데이트
   */
  int updateStatus(
      @Param("sessionId") Long sessionId,
      @Param("userName") String userName,
      @Param("status") String status,
      @Param("memo") String memo
  );

  /**
   * 출석 정보 생성 (단일)
   */
  int insertAttendance(@Param("attendance") TrainingAttendance attendance);

  /**
   * 신청 ID로 출석 정보 생성
   */
  int insertAttendanceByApplicationId(
      @Param("applicationId") Long applicationId,
      @Param("createdBy") Long createdBy
  );

  /**
   * 여러 신청 ID로 출석 정보 일괄 생성
   */
  int insertAttendanceByApplicationIds(
      @Param("applicationIds") List<Long> applicationIds,
      @Param("createdBy") Long createdBy
  );
}