package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수업 시작 마감 처리 서비스
 *
 * <p>스케줄러에서 호출되는 실제 비즈니스 로직을 담당합니다.
 * Self-invocation 문제를 방지하기 위해 별도 컴포넌트로 분리했습니다.
 *
 * @author GitHub Copilot
 * @since 2025-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionDeadlineService {

    private final TrainingSessionDAO trainingSessionDAO;
    private final ApplicationDAO applicationDAO;
    private final TrainerUserDAO trainerUserDAO;
    /**
     * 수업 시작 마감 처리 실제 로직 (트랜잭션 적용)
     *
     * <p>이 메서드는 별도의 트랜잭션으로 실행되며, 예외 발생 시 자동으로 롤백됩니다.
     *
     * @param sessionDeadlineHours 마감 시간 (시간 단위)
     */
    @Transactional(rollbackFor = Exception.class)
    public void processSessionDeadline(int sessionDeadlineHours) {
        // 1. 마감 시간이 지난 신청 조회
        List<Long> expiredApplicationIds =
            trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours);

        if (expiredApplicationIds == null || expiredApplicationIds.isEmpty()) {
            log.info("마감 대상 신청 없음");
            return;
        }

        log.info("마감 대상 신청 {}건 발견", expiredApplicationIds.size());
        log.debug("마감 대상 신청 ID 목록: {}", expiredApplicationIds);

        // 2. 일괄 EXPIRED 처리
        applicationDAO.updateApplicationStatusBatch(expiredApplicationIds, "EXPIRED");

        log.info("수업 시작 마감 처리 완료 - {}건 처리", expiredApplicationIds.size());

    }
}

