package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.training.dao.CourseDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 훈련 과정 상태 자동 업데이트 스케줄러
 *
 * <p>주기: 10분마다 실행
 * <p>기능:
 * <ul>
 *   <li>첫 번째 세션이 시작된 과정: SCHEDULED → IN_PROGRESS</li>
 *   <li>모든 세션이 종료된 과정: IN_PROGRESS → DONE</li>
 *   <li>진행중(IN_PROGRESS) 과정은 신청 불가</li>
 * </ul>
 *
 * @author GitHub Copilot
 * @since 2025-01-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseStatusScheduler {

    private final CourseDAO courseDAO;

    /**
     * 기능 활성화 플래그
     * application.yml의 course.status.update.enabled 값 사용
     * 기본값: true (활성화)
     */
    @Value("${course.status.update.enabled:true}")
    private boolean courseStatusUpdateEnabled;

    /**
     * 10분마다 훈련 과정 상태 업데이트
     *
     * <p>cron 표현식: "0 6/10 * * * *"
     * <ul>
     *   <li>초: 0</li>
     *   <li>분: 6, 16, 26, 36, 46, 56 (10분마다, 6분 오프셋)</li>
     *   <li>시: * (매시간)</li>
     *   <li>일: * (매일)</li>
     *   <li>월: * (매월)</li>
     *   <li>요일: * (매일)</li>
     * </ul>
     * <p>다른 스케줄러와 실행 시간 분산:
     * <ul>
     *   <li>SessionDeadlineScheduler: 0분, 10분, 20분...</li>
     *   <li>PaymentDeadlineScheduler: 3분, 13분, 23분...</li>
     *   <li>CourseStatusScheduler: 6분, 16분, 26분...</li>
     * </ul>
     */
    @Scheduled(cron = "0 6/10 * * * *")
    @Transactional
    public void updateCourseStatus() {
        // 기능 비활성화 체크
        if (!courseStatusUpdateEnabled) {
            log.debug("훈련 과정 상태 업데이트 기능이 비활성화되어 있습니다.");
            return;
        }

        log.info("=== 훈련 과정 상태 업데이트 시작 ===");

        try {
            // 1. SCHEDULED → IN_PROGRESS (첫 세션 시작됨)
            int inProgressCount = courseDAO.updateCourseStatusToInProgress();
            if (inProgressCount > 0) {
                log.info("진행중으로 변경된 과정: {}건", inProgressCount);
            }

            // 2. IN_PROGRESS → DONE (모든 세션 종료됨)
            int completedCount = courseDAO.updateCourseStatusToCompleted();
            if (completedCount > 0) {
                log.info("종료로 변경된 과정: {}건", completedCount);
            }

            if (inProgressCount == 0 && completedCount == 0) {
                log.debug("상태 변경이 필요한 과정이 없습니다.");
            }

        } catch (Exception e) {
            log.error("훈련 과정 상태 업데이트 중 오류 발생 - 다음 스케줄링 시 재시도", e);
            // 예외를 다시 던지지 않음 - 스케줄러가 중단되지 않도록 함
        }

        log.info("=== 훈련 과정 상태 업데이트 종료 ===");
    }
}

