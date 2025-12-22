package com.mungtrainer.mtserver.training.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceListResponse {

    private Long attendanceId;
    private String userName;
    private String dogName;
    private String dogProfileImage;  // 반려견 프로필 이미지 (S3 key → Presigned URL)
    private String status;  // ATTENDED, ABSENT, LATE, EXCUSED
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private String memo;
    private LocalDateTime createdAt;
}