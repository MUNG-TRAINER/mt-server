package com.mungtrainer.mtserver.training.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 훈련 코스 공지 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingCourseNotice extends BaseEntity {

    /**
     * 공지 ID (PK)
     */
    private Long noticeId;

    /**
     * 세션 ID (FK - training_session.session_id)
     */
    private Long sessionId;

    /**
     * 대상 사용자 ID (FK - user.user_id)
     */
    private Long targetUserId;

    /**
     * 대상 유형 (ALL, USER)
     */
    private String targetType;

    /**
     * 내용
     */
    private String content;
}

