package com.mungtrainer.mtserver.training.service;

import com.mungtrainer.mtserver.common.exception.CustomException;
import com.mungtrainer.mtserver.common.exception.ErrorCode;
import com.mungtrainer.mtserver.training.dto.request.UpdateSessionRequest;
import com.mungtrainer.mtserver.training.dto.response.TrainingSessionResponse;
import com.mungtrainer.mtserver.training.dao.TrainingSessionDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingSessionService {

    private final TrainingSessionDAO trainingSessionMapper;

    /**
     * 특정 코스의 세션 목록 조회
     */
    public List<TrainingSessionResponse> getSessionsByCourseId(Long courseId) {
        return trainingSessionMapper.findSessionsByCourseId(courseId);
    }

    /**
     * 특정 세션 상세 조회
     */
    public TrainingSessionResponse getSessionById(Long courseId, Long sessionId) {
        TrainingSessionResponse session = trainingSessionMapper.findSessionById(courseId, sessionId);

        if (session == null) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        return session;
    }

    /**
     * 세션 수정
     */
    @Transactional
    public void updateSession(Long sessionId, Long trainerId, UpdateSessionRequest request) {
        validateTrainerOwnership(sessionId, trainerId);

        if (isEmptyUpdateRequest(request)) {
            throw new CustomException(ErrorCode.SESSION_EMPTY_UPDATE);
        }

        try {
          int deletedCount = trainingSessionMapper.deleteSession(sessionId);
          if (deletedCount == 0) {
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
          }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_UPDATE_FAILED);
        }
    }

    /**
     * 세션 삭제
     */
    @Transactional
    public void deleteSession(Long sessionId, Long trainerId) {
        validateTrainerOwnership(sessionId, trainerId);

        Boolean hasApplications = trainingSessionMapper.hasActiveApplications(sessionId);
        if (hasApplications != null && hasApplications) {
            throw new CustomException(ErrorCode.SESSION_HAS_APPLICATIONS);
        }

        try {
            trainingSessionMapper.deleteSession(sessionId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_DELETE_FAILED);
        }
    }

    /**
     * 훈련사 권한 확인
     */
    private void validateTrainerOwnership(Long sessionId, Long trainerId) {
        Boolean isOwner = trainingSessionMapper.isSessionOwnedByTrainer(sessionId, trainerId);

        if (isOwner == null || !isOwner) {
            throw new CustomException(ErrorCode.SESSION_NO_PERMISSION);
        }
    }

    /**
     * 수정 요청이 비어있는지 확인
     */
    private boolean isEmptyUpdateRequest(UpdateSessionRequest request) {
        return request.getSessionDate() == null
            && request.getStartTime() == null
            && request.getEndTime() == null
            && isBlankOrNull(request.getLocationDetail())
            && isBlankOrNull(request.getStatus())
            && request.getMaxStudents() == null
            && isBlankOrNull(request.getContent())
            && request.getPrice() == null;
    }

    /**
     * 문자열이 null이거나 빈 문자열인지 확인
     */
    private boolean isBlankOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
}