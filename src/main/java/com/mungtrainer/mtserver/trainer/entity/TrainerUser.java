package com.mungtrainer.mtserver.trainer.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 훈련사-사용자 관계 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainerUser extends BaseEntity {

    /**
     * 훈련사 ID (PK, FK - trainer_profile.trainer_id)
     */
    private Long trainerId;

    /**
     * 사용자 ID (FK - user.user_id)
     */
    private Long userId;
}

