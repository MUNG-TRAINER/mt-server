package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.training.dao.CourseDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 훈련 과정 상태 업데이트 서비스
 *
 * <p>CourseStatusScheduler에서 사용하는 트랜잭션 처리 서비스
 * <p>각 업데이트 작업을 별도 트랜잭션으로 실행하여 부분 실패 허용
 *
 * @author GitHub Copilot
 * @since 2025-01-29
 */
@Service
@RequiredArgsConstructor
public class CourseStatusUpdateService {

    private final CourseDAO courseDAO;
    private final TrainingSessionDAO trainingSessionDAO;

    /**
     * 세션 상태를 DONE으로 변경 (별도 트랜잭션)
     *
     * <p>종료 시간이 지난 세션을 DONE으로 변경
     *
     * @return 변경된 세션 수
     */
    @Transactional
    public int updateSessionToDone() {
        return trainingSessionDAO.updateSessionStatusToDone();
    }

    /**
     * 과정 상태를 IN_PROGRESS로 변경 (별도 트랜잭션)
     *
     * <p>SCHEDULED 상태의 과정 중 첫 세션이 시작된 과정을 IN_PROGRESS로 변경
     *
     * @return 변경된 과정 수
     */
    @Transactional
    public int updateToInProgress() {
        return courseDAO.updateCourseStatusToInProgress();
    }

    /**
     * 과정 상태를 DONE으로 변경 (별도 트랜잭션)
     *
     * <p>IN_PROGRESS 상태의 과정 중 모든 세션이 종료된 과정을 DONE으로 변경
     *
     * @return 변경된 과정 수
     */
    @Transactional
    public int updateToCompleted() {
        return courseDAO.updateCourseStatusToCompleted();
    }
}

