package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.entity.TrainingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 10분마다 결제 기한 만료 처리
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 */10 * * * *")  // 매 10분마다
    @Transactional
    public void processExpiredPayments() {
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

            // 4. 사용자에게 알림 발송
            // TODO: notificationService.sendToUser(
            //     applicationId,
            //     "결제 기한 만료",
            //     "결제 기한이 지나 신청이 만료되었습니다. 다시 신청해주세요."
            // );

        } catch (Exception e) {
            log.error("신청 만료 처리 실패 - applicationId: {}", applicationId, e);
        }
    }

    /**
     * 대기자 자동 승격 (TrainerUserService의 로직과 동일)
     */
    private void promoteNextWaiting(Long sessionId) {
        log.info("대기자 승격 시작 - sessionId: {}", sessionId);

        // 1. 세션 정보 조회
        TrainingSession session = trainerUserDao.findSessionById(sessionId);
        if (session == null) {
            log.warn("세션을 찾을 수 없음 - sessionId: {}", sessionId);
            return;
        }

        // 2. 현재 승인된 인원 확인
        int currentCount = trainerUserDao.countApprovedApplications(sessionId);

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
        trainerUserDao.updatePaymentDeadline(nextApplicationId, 24); // 24시간

        log.info("대기자 자동 승격 완료 - applicationId: {}, 결제 기한: 24시간", nextApplicationId);

        // 5. 사용자에게 결제 안내 알림
        // TODO: notificationService.sendToUser(
        //     nextApplicationId,
        //     "승인 완료",
        //     "대기가 해제되었습니다! 24시간 내에 결제를 완료해주세요.",
        //     "/payments/" + nextApplicationId
        // );
    }
}

