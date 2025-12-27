package com.mungtrainer.mtserver.training.scheduler;

import com.mungtrainer.mtserver.counseling.dao.TrainerUserDAO;
import com.mungtrainer.mtserver.notification.entity.NotificationCommand;
import com.mungtrainer.mtserver.notification.entity.TrainingApplicationNotificationFactory;
import com.mungtrainer.mtserver.notification.service.NotificationService;
import com.mungtrainer.mtserver.training.dao.ApplicationDAO;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SessionDeadlineService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("세션 마감 처리 서비스 테스트")
class SessionDeadlineServiceTest {

    @Mock
    private TrainingSessionDAO trainingSessionDAO;

    @Mock
    private ApplicationDAO applicationDAO;

    @Mock
    private TrainerUserDAO trainerUserDAO;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TrainingApplicationNotificationFactory trainingApplicationNotificationFactory;

    @InjectMocks
    private SessionDeadlineService sessionDeadlineService;

    @Test
    @DisplayName("마감 대상 신청이 없을 때 정상 처리되어야 한다")
    void processSessionDeadline_NoExpiredApplications_ShouldReturnEarly() {
        // Given
        int sessionDeadlineHours = 24;
        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(Collections.emptyList());

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(sessionDeadlineHours);
        verify(applicationDAO, never()).updateApplicationStatusBatch(anyList(), anyString());
        verify(notificationService, never()).send(any(NotificationCommand.class));
    }

    @Test
    @DisplayName("마감 대상 신청이 null일 때 정상 처리되어야 한다")
    void processSessionDeadline_NullExpiredApplications_ShouldReturnEarly() {
        // Given
        int sessionDeadlineHours = 24;
        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(null);

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(sessionDeadlineHours);
        verify(applicationDAO, never()).updateApplicationStatusBatch(anyList(), anyString());
        verify(notificationService, never()).send(any(NotificationCommand.class));
    }

    @Test
    @DisplayName("마감 대상 신청이 있을 때 상태를 EXPIRED로 변경하고 알림을 발송해야 한다")
    void processSessionDeadline_WithExpiredApplications_ShouldUpdateAndSendNotifications() {
        // Given
        int sessionDeadlineHours = 24;
        List<Long> expiredApplicationIds = Arrays.asList(1L, 2L, 3L);
        Long userId1 = 100L;
        Long userId2 = 200L;
        Long userId3 = 300L;

        NotificationCommand mockCommand = NotificationCommand.builder()
            .targetUserId(userId1)
            .type("SESSION_DEADLINE_EXPIRED")
            .title("수업 시작으로 신청 마감")
            .message("수업이 시작되어 신청이 마감되었습니다.")
            .build();

        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(expiredApplicationIds);
        when(trainerUserDAO.findUserIdByApplicationId(1L)).thenReturn(userId1);
        when(trainerUserDAO.findUserIdByApplicationId(2L)).thenReturn(userId2);
        when(trainerUserDAO.findUserIdByApplicationId(3L)).thenReturn(userId3);
        when(trainingApplicationNotificationFactory.sessionDeadlineExpired(anyLong(), anyLong()))
            .thenReturn(mockCommand);

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(trainingSessionDAO).findApplicationsPastSessionDeadline(sessionDeadlineHours);
        verify(applicationDAO).updateApplicationStatusBatch(expiredApplicationIds, "EXPIRED");
        verify(trainerUserDAO, times(3)).findUserIdByApplicationId(anyLong());
        verify(trainingApplicationNotificationFactory, times(3))
            .sessionDeadlineExpired(anyLong(), anyLong());
        verify(notificationService, times(3)).send(any(NotificationCommand.class));

        // 각 신청에 대해 올바른 파라미터로 호출되었는지 검증
        verify(trainingApplicationNotificationFactory).sessionDeadlineExpired(userId1, 1L);
        verify(trainingApplicationNotificationFactory).sessionDeadlineExpired(userId2, 2L);
        verify(trainingApplicationNotificationFactory).sessionDeadlineExpired(userId3, 3L);
    }

    @Test
    @DisplayName("userId가 null인 경우 알림을 발송하지 않아야 한다")
    void processSessionDeadline_WhenUserIdIsNull_ShouldNotSendNotification() {
        // Given
        int sessionDeadlineHours = 24;
        List<Long> expiredApplicationIds = Arrays.asList(1L, 2L);

        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(expiredApplicationIds);
        when(trainerUserDAO.findUserIdByApplicationId(1L)).thenReturn(null); // userId가 null
        when(trainerUserDAO.findUserIdByApplicationId(2L)).thenReturn(200L);

        NotificationCommand mockCommand = NotificationCommand.builder()
            .targetUserId(200L)
            .type("SESSION_DEADLINE_EXPIRED")
            .title("수업 시작으로 신청 마감")
            .message("수업이 시작되어 신청이 마감되었습니다.")
            .build();

        when(trainingApplicationNotificationFactory.sessionDeadlineExpired(200L, 2L))
            .thenReturn(mockCommand);

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(applicationDAO).updateApplicationStatusBatch(expiredApplicationIds, "EXPIRED");
        verify(notificationService, times(1)).send(any(NotificationCommand.class)); // 1건만 발송
        verify(notificationService).send(mockCommand);
    }

    @Test
    @DisplayName("알림 발송 중 예외가 발생해도 다른 알림은 계속 발송되어야 한다")
    void processSessionDeadline_WhenNotificationFails_ShouldContinueOtherNotifications() {
        // Given
        int sessionDeadlineHours = 24;
        List<Long> expiredApplicationIds = Arrays.asList(1L, 2L, 3L);

        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(expiredApplicationIds);
        when(trainerUserDAO.findUserIdByApplicationId(1L)).thenReturn(100L);
        when(trainerUserDAO.findUserIdByApplicationId(2L)).thenReturn(200L);
        when(trainerUserDAO.findUserIdByApplicationId(3L)).thenReturn(300L);

        NotificationCommand mockCommand = NotificationCommand.builder()
            .targetUserId(100L)
            .type("SESSION_DEADLINE_EXPIRED")
            .title("수업 시작으로 신청 마감")
            .message("수업이 시작되어 신청이 마감되었습니다.")
            .build();

        when(trainingApplicationNotificationFactory.sessionDeadlineExpired(anyLong(), anyLong()))
            .thenReturn(mockCommand);

        // 두 번째 알림 발송 시 예외 발생
        doThrow(new RuntimeException("알림 발송 실패"))
            .when(notificationService).send(any(NotificationCommand.class));

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(applicationDAO).updateApplicationStatusBatch(expiredApplicationIds, "EXPIRED");
        verify(notificationService, times(3)).send(any(NotificationCommand.class)); // 3번 시도
        verify(trainingApplicationNotificationFactory, times(3))
            .sessionDeadlineExpired(anyLong(), anyLong());
    }

    @Test
    @DisplayName("단일 신청 마감 처리 시 정상 동작해야 한다")
    void processSessionDeadline_WithSingleApplication_ShouldWork() {
        // Given
        int sessionDeadlineHours = 24;
        List<Long> expiredApplicationIds = Collections.singletonList(1L);
        Long userId = 100L;

        NotificationCommand mockCommand = NotificationCommand.builder()
            .targetUserId(userId)
            .type("SESSION_DEADLINE_EXPIRED")
            .title("수업 시작으로 신청 마감")
            .message("수업이 시작되어 신청이 마감되었습니다.")
            .referenceId(1L)
            .referenceType("TRAINING_APPLICATION")
            .actionUrl("/training/application/1")
            .actorId(0L)
            .build();

        when(trainingSessionDAO.findApplicationsPastSessionDeadline(sessionDeadlineHours))
            .thenReturn(expiredApplicationIds);
        when(trainerUserDAO.findUserIdByApplicationId(1L)).thenReturn(userId);
        when(trainingApplicationNotificationFactory.sessionDeadlineExpired(userId, 1L))
            .thenReturn(mockCommand);

        // When
        sessionDeadlineService.processSessionDeadline(sessionDeadlineHours);

        // Then
        verify(applicationDAO).updateApplicationStatusBatch(expiredApplicationIds, "EXPIRED");
        verify(notificationService).send(mockCommand);
    }
}

