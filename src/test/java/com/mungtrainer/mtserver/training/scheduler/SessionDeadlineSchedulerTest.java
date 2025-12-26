package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("수업 시작 마감 스케줄러 테스트")
class SessionDeadlineSchedulerTest {

    @Mock
    private TrainingSessionDAO trainingSessionDAO;

    @Mock
    private ApplicationDAO applicationDAO;

    @InjectMocks
    private SessionDeadlineScheduler scheduler;

    @BeforeEach
    void setUp() {
        // 설정 값 주입
        ReflectionTestUtils.setField(scheduler, "sessionDeadlineHours", 24);
        ReflectionTestUtils.setField(scheduler, "sessionDeadlineEnabled", true);
    }

    @Test
    @DisplayName("마감 대상 신청이 없으면 처리하지 않음")
    void processSessionDeadline_NoExpiredApplications() {
        // Given
        when(trainingSessionDAO.findApplicationsPastSessionDeadline(24))
            .thenReturn(Collections.emptyList());

        // When
        scheduler.processSessionDeadline();

        // Then
        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(24);
        verify(applicationDAO, never()).updateApplicationStatusBatch(anyList(), anyString());
    }

    @Test
    @DisplayName("마감 대상 신청이 있으면 EXPIRED 처리")
    void processSessionDeadline_WithExpiredApplications() {
        // Given
        List<Long> expiredIds = Arrays.asList(1L, 2L, 3L);
        when(trainingSessionDAO.findApplicationsPastSessionDeadline(24))
            .thenReturn(expiredIds);

        // When
        scheduler.processSessionDeadline();

        // Then
        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(24);
        verify(applicationDAO).updateApplicationStatusBatch(expiredIds, "EXPIRED");
    }

    @Test
    @DisplayName("기능이 비활성화되면 처리하지 않음")
    void processSessionDeadline_Disabled() {
        // Given
        ReflectionTestUtils.setField(scheduler, "sessionDeadlineEnabled", false);

        // When
        scheduler.processSessionDeadline();

        // Then
        verify(trainingSessionDAO, never()).findApplicationsPastSessionDeadline(anyInt());
        verify(applicationDAO, never()).updateApplicationStatusBatch(anyList(), anyString());
    }

    @Test
    @DisplayName("예외 발생 시 다음 스케줄링을 위해 예외를 삼킴")
    void processSessionDeadline_ExceptionHandling() {
        // Given
        when(trainingSessionDAO.findApplicationsPastSessionDeadline(24))
            .thenThrow(new RuntimeException("DB 오류"));

        // When & Then (예외가 밖으로 전파되지 않아야 함)
        scheduler.processSessionDeadline();

        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(24);
    }
}

