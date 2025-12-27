package com.mungtrainer.mtserver.counseling.dao;

import com.mungtrainer.mtserver.counseling.dto.response.*;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TrainerUserDAO {

    // 유저 ID로 연결된 훈련사 ID 조회
    Long findTrainerIdByUserId(@Param("userId") Long userId);

    // 훈련사와 사용자 관계 존재 여부 확인
    boolean existsTrainerUserRelation(
            @Param("trainerId") Long trainerId,
            @Param("userId") Long userId
    );
    // dog Id 로 훈련사 Id 조회
    Long findTrainerIdByDogId(@Param("dogId") Long dogId);


    // 회원 목록 조회
    List<TrainerUserListResponse> findUsersByTrainerId(@Param("trainerId") Long trainerId);
    // 반려견 신청 과정 조회 (출석 미포함, tags 기준 회차 계산)
    List<TrainingApplicationResponse> findTrainingApplicationsByDogId(@Param("dogId") Long dogId);

    List<MultiCourseGroupResponse> findMultiCoursesByDogId(Long dogId);

    List<MultiSessionResponse> findSessionsWithAttendance(
            @Param("dogId") Long dogId,
            @Param("courseId") Long courseId
    );

    int countSessionsByCourseId(Long courseId);

    // 출석한 세션 수 조회 (추가)
    int countAttendedSessions(
            @Param("dogId") Long dogId,
            @Param("courseId") Long courseId
    );

    List<AppliedWaitingResponse> selectWaitingApplications(@Param("trainerId") Long trainerId);

    // 코스별로 그룹핑된 승인 대기 목록 조회
    List<GroupedApplicationResponse> selectGroupedWaitingApplications(@Param("trainerId") Long trainerId);

    ApplicationDogDetailResponse selectApplicationDogDetail(
            @Param("applicationId") Long applicationId,
            @Param("trainerId") Long trainerId
    );

    int updateStatusApproved(@Param("applicationId") Long applicationId,
                             @Param("trainerId") Long trainerId);

    int updateStatusRejected(@Param("applicationId") Long applicationId,
                             @Param("trainerId") Long trainerId,
                             @Param("rejectReason") String rejectReason);

    // 일괄 승인 (코스의 모든 회차)
    int updateBulkStatusApproved(@Param("courseId") Long courseId,
                                 @Param("dogId") Long dogId,
                                 @Param("trainerId") Long trainerId);

    // 일괄 거절 (코스의 모든 회차)
    int updateBulkStatusRejected(@Param("courseId") Long courseId,
                                 @Param("dogId") Long dogId,
                                 @Param("trainerId") Long trainerId,
                                 @Param("rejectReason") String rejectReason);

    List<MultiCourseGroupResponse> findMultiCourseDetail(Map<String, Long> params);

    /**
     * 코스와 반려견으로 신청 ID 목록 조회
     * 일괄 승인 시 출석 정보 생성에 사용
     */
    List<Long> findApplicationIdsByCourseAndDog(@Param("courseId") Long courseId,
                                                 @Param("dogId") Long dogId);

    /**
     * ========================================
     * 대기 로직 개선을 위한 메서드 추가
     * ========================================
     */

    /**
     * 신청 ID로 사용자 ID 조회
     * @param applicationId 신청 ID
     * @return 사용자 ID
     */
    Long findUserIdByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 신청 ID로 세션 ID 조회
     * @param applicationId 신청 ID
     * @return 세션 ID
     */
    Long findSessionIdByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * 세션 조회
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    TrainingSession findSessionById(@Param("sessionId") Long sessionId);

    /**
     * 세션 조회 (비관적 락)
     * SELECT FOR UPDATE를 사용하여 동시성 제어
     * 대기자 승격 시 경쟁 상태 방지를 위해 사용
     *
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    TrainingSession findSessionByIdForUpdate(@Param("sessionId") Long sessionId);

    /**
     * 승인된 신청 수 카운트 (ACCEPT, PAID만)
     * @param sessionId 세션 ID
     * @return 승인된 신청 수
     */
    int countApprovedApplications(@Param("sessionId") Long sessionId);

    /**
     * 가장 오래 대기한 신청 ID 조회 (FIFO)
     * @param sessionId 세션 ID
     * @return 대기 중인 신청 ID (없으면 null)
     */
    Long findOldestWaitingApplicationId(@Param("sessionId") Long sessionId);

    /**
     * 신청 상태 단순 업데이트 (감사 정보 제외)
     * @param applicationId 신청 ID
     * @param status 변경할 상태
     */
    void updateApplicationStatusSimple(
        @Param("applicationId") Long applicationId,
        @Param("status") String status
    );

    /**
     * 대기 상태 업데이트
     * @param applicationId 신청 ID
     * @param status 변경할 상태 (PROMOTED, CANCELLED 등)
     */
    void updateWaitingStatus(
        @Param("applicationId") Long applicationId,
        @Param("status") String status
    );

    /**
     * 대기 테이블 등록
     * @param applicationId 신청 ID
     * @param userId 생성자 ID
     */
    void insertWaiting(
        @Param("applicationId") Long applicationId,
        @Param("userId") Long userId
    );

    /**
     * ========================================
     * 결제 기한 관리용 메서드 (선택 기능)
     * ========================================
     */

    /**
     * 결제 기한 설정
     * @param applicationId 신청 ID
     * @param hours 기한 (시간)
     */
    void updatePaymentDeadline(
        @Param("applicationId") Long applicationId,
        @Param("hours") int hours
    );

    /**
     * 결제 기한이 지난 ACCEPT 상태 신청 조회
     * @return 만료된 신청 ID 목록
     */
    List<Long> findExpiredAcceptApplications();

    /**
     * 신청을 만료 처리 (ACCEPT → EXPIRED)
     * @param applicationId 신청 ID
     */
    void expireApplication(@Param("applicationId") Long applicationId);

    /**
     * 결제 기한 초기화 (결제 완료 시)
     * @param applicationId 신청 ID
     */
    void clearPaymentDeadline(@Param("applicationId") Long applicationId);
}
