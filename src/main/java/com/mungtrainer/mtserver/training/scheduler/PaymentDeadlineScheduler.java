package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 기한 만료 자동 처리 스케줄러
 *
 * 주기: 10분마다 실행
 * 기능:
 * 1. 결제 기한이 지난 ACCEPT 상태 신청 조회
 * 2. EXPIRED 상태로 변경
 * 3. 해당 세션의 다음 대기자 자동 승격
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentDeadlineScheduler {

    private final TrainerUserDAO trainerUserDao;

    /**
     * 결제 기한 (시간)
     * application.yml의 payment.deadline.hours 값 사용
     * 기본값: 24시간
     */
    @Value("${payment.deadline.hours:24}")
    private int paymentDeadlineHours;

    /**
     * 기능 활성화 플래그
     * application.yml의 payment.deadline.enabled 값 사용
     * 기본값: true (활성화)
     */
    @Value("${payment.deadline.enabled:true}")
    private boolean paymentDeadlineEnabled;

    /**
     * 10분마다 결제 기한 만료 처리
     * cron: 초 분 시 일 월 요일
     * 실행 시간: 3분, 13분, 23분, 33분, 43분, 53분 (SessionDeadlineScheduler와 시간 분산)
     */
    @Scheduled(cron = "0 3/10 * * * *")  // 매 10분마다 (3분부터 시작)
    @Transactional
    public void processExpiredPayments() {
        // 기능 비활성화 체크 (긴급 롤백용)
        if (!paymentDeadlineEnabled) {
            log.debug("결제 기한 만료 기능이 비활성화되어 있습니다.");
            return;
        }

        log.info("결제 기한 만료 처리 시작");

        try {
            // 1. 만료된 신청 조회
            List<Long> expiredApplicationIds = trainerUserDao.findExpiredAcceptApplications();

            if (expiredApplicationIds == null || expiredApplicationIds.isEmpty()) {
                log.info("만료된 신청 없음");
                return;
            }

            log.info("만료된 신청 {}건 발견", expiredApplicationIds.size());

            // 2. 각 신청 처리
            for (Long applicationId : expiredApplicationIds) {
                processExpiredApplication(applicationId);
            }

            log.info("결제 기한 만료 처리 완료 - {}건 처리", expiredApplicationIds.size());

        } catch (Exception e) {
            log.error("결제 기한 만료 처리 중 오류 발생", e);
        }
    }

    /**
     * 개별 만료 신청 처리
     */
    private void processExpiredApplication(Long applicationId) {
        try {
            log.info("신청 만료 처리 - applicationId: {}", applicationId);

            // 1. 세션 ID 조회
            Long sessionId = trainerUserDao.findSessionIdByApplicationId(applicationId);
            if (sessionId == null) {
                log.warn("세션 ID를 찾을 수 없음 - applicationId: {}", applicationId);
                return;
            }

            // 2. EXPIRED 상태로 변경
            trainerUserDao.expireApplication(applicationId);
            log.info("신청 만료 완료 - applicationId: {}", applicationId);

            // 3. 다음 대기자 승격
            promoteNextWaiting(sessionId);


        } catch (Exception e) {
            log.error("신청 만료 처리 실패 - applicationId: {}", applicationId, e);
        }
    }

    /**
     * 대기자 자동 승격 (TrainerUserService의 로직과 동일)
     *
     *  동시성 제어: SELECT FOR UPDATE를 사용하여 세션에 락을 걸어
     *                 여러 트랜잭션이 동시에 승격을 시도해도 정원 초과가 발생하지 않습니다.
     */
    private void promoteNextWaiting(Long sessionId) {
        log.info("대기자 승격 시작 - sessionId: {}", sessionId);

        // 1.  세션 정보 조회 (비관적 락)
        //    SELECT FOR UPDATE로 행 레벨 락 획득
        TrainingSession session = trainerUserDao.findSessionByIdForUpdate(sessionId);
        if (session == null) {
            log.warn("세션을 찾을 수 없음 - sessionId: {}", sessionId);
            return;
        }

        log.debug("세션 락 획득 완료 - sessionId: {}, 정원: {}", sessionId, session.getMaxStudents());

        // 2.  현재 승인된 인원 확인 (락 획득 후 다시 확인)
        int currentCount = trainerUserDao.countApprovedApplications(sessionId);

        log.info("정원 확인 (락 획득 후) - 현재: {}/{}, sessionId: {}",
                 currentCount, session.getMaxStudents(), sessionId);

        if (currentCount >= session.getMaxStudents()) {
            log.info("정원이 가득 차서 대기자를 승격하지 않습니다. - sessionId: {}", sessionId);
            return;
        }

        // 3. 가장 오래 대기한 사람 조회
        Long nextApplicationId = trainerUserDao.findOldestWaitingApplicationId(sessionId);

        if (nextApplicationId == null) {
            log.info("대기 중인 신청이 없습니다. - sessionId: {}", sessionId);
            return;
        }

        // 4. WAITING → ACCEPT로 변경 + 결제 기한 설정
        trainerUserDao.updateApplicationStatusSimple(nextApplicationId, "ACCEPT");
        trainerUserDao.updateWaitingStatus(nextApplicationId, "PROMOTED");
        trainerUserDao.updatePaymentDeadline(nextApplicationId, paymentDeadlineHours);

        log.info("대기자 자동 승격 완료 - applicationId: {}, 결제 기한: {}시간", nextApplicationId, paymentDeadlineHours);
        
    }
}

