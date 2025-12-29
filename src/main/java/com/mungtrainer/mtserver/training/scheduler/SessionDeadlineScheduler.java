package com.mungtrainer.mtserver.training.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 수업 시작 마감 자동 처리 스케줄러
 *
 * <p>주기: 10분마다 실행
 * <p>기능:
 * <ul>
 *   <li>수업 시작 24시간 전이 지난 세션 조회</li>
 *   <li>해당 세션의 미승인 신청(APPLIED, COUNSELING_REQUIRED, WAITING) EXPIRED 처리</li>
 *   <li>ACCEPT 상태는 제외 (결제 기한 마감 스케줄러에서 처리)</li>
 * </ul>
 *
 * @author GitHub Copilot
 * @since 2025-12-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionDeadlineScheduler {

    private final SessionDeadlineService sessionDeadlineService;

    /**
     * 마감 시간 (시간)
     * application.yml의 session.deadline.hours 값 사용
     * 기본값: 24시간
     */
    @Value("${session.deadline.hours:24}")
    private int sessionDeadlineHours;

    /**
     * 기능 활성화 플래그
     * application.yml의 session.deadline.enabled 값 사용
     * 기본값: true (활성화)
     */
    @Value("${session.deadline.enabled:true}")
    private boolean sessionDeadlineEnabled;

    /**
     * 10분마다 수업 시작 마감 처리 (스케줄러 진입점)
     *
     * <p>cron 표현식: "0 * /10 * * * *"
     * <ul>
     *   <li>초: 0</li>
     *   <li>분: * /10 (10분마다)</li>
     *   <li>시: * (매시간)</li>
     *   <li>일: * (매일)</li>
     *   <li>월: * (매월)</li>
     *   <li>요일: * (매일)</li>
     * </ul>
     *
     * <p>주의: 이 메서드는 예외 처리만 담당합니다.
     * 실제 트랜잭션 로직은 {@link SessionDeadlineService#processSessionDeadline(int)}에서 수행됩니다.
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void processSessionDeadline() {
        // 기능 비활성화 체크 (긴급 롤백용)
        if (!sessionDeadlineEnabled) {
            log.debug("수업 시작 마감 기능이 비활성화되어 있습니다.");
            return;
        }

        log.info("=== 수업 시작 마감 처리 시작 ===");

        try {
            // 실제 처리 로직 호출 (트랜잭션 적용)
            sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        } catch (Exception e) {
            log.error("수업 시작 마감 처리 중 오류 발생 - 다음 스케줄링 시 재시도", e);
            // 예외를 다시 던지지 않음 - 스케줄러가 중단되지 않도록 함
        }

        log.info("=== 수업 시작 마감 처리 종료 ===");
    }
}

