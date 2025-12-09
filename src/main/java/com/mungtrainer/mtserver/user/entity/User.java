package com.mungtrainer.mtserver.user.entity;

import com.mungtrainer.mtserver.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {
  private Long userId;
  // 추가 필드들...
}