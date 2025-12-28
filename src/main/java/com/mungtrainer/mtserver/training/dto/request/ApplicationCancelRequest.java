package com.mungtrainer.mtserver.training.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationCancelRequest {
    private List<Long> courseIds;
    private Long dogId; // 특정 반려견의 신청만 취소하기 위한 필드
}
