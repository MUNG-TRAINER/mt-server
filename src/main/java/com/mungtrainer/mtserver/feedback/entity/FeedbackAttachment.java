package com.mungtrainer.mtserver.feedback.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 피드백 첨부파일 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FeedbackAttachment extends BaseEntity {

    /**
     * 첨부파일 ID (PK)
     */
    private Long attachmentId;

    /**
     * 피드백 ID (FK - feedback.feedback_id)
     */
    private Long feedbackId;

    /**
     * 파일 URL
     */
    private String fileUrl;
}

