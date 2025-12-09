package com.mungtrainer.mtserver.feedback.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 피드백 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Feedback extends BaseEntity {

    /**
     * 피드백 ID (PK)
     */
    private Long feedbackId;

    /**
     * 신청 ID (FK - training_course_application.application_id)
     */
    private Long applicationId;

    /**
     * 작성자 ID (FK - user.user_id)
     */
    private Long writerId;

    /**
     * 수신자 ID (FK - user.user_id)
     */
    private Long receiverId;

    /**
     * 내용
     */
    private String content;

    /**
     * 유형 (GIVE, TAKE, FEEDBACK)
     */
    private String type;
}

