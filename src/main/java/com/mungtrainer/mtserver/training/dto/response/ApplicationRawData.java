package com.mungtrainer.mtserver.training.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 매퍼에서 조회한 신청 정보 (세션 단위)
 * 서비스에서 과정 단위로 그룹핑하여 ApplicationListViewResponse로 변환
 */
@Getter
@Setter
public class ApplicationRawData {
    // 신청 식별
    private Long applicationId;
    private Long dogId;

    // 신청 상태
    private String applicationStatus;
    private String rejectReason;

    // 코스 정보
    private Long courseId;
    private String tags;
    private String title;
    private String description;
    private String mainImage;
    private String location;
    private String lessonForm;
    private String type;

    // 세션 정보
    private String sessionSchedule;
    private Integer price;

    // 반려견 정보
    private String dogName;
}

