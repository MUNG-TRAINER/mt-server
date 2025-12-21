package com.mungtrainer.mtserver.training.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBaseRequest {
    @Pattern(regexp = "^(ONCE|MULTI)$", message = "type은 ONCE 또는 MULTI만 가능합니다.")
    private String type;
    @Pattern(regexp = "^(WALK|GROUP|PRIVATE)$", message = "lessonForm은 WALK, GROUP, PRIVATE 중 하나여야 합니다.")
    private String lessonForm;
    @NotNull(message = "sessionUploadRequests는 필수입니다.")
    @Size(min = 1, message = "최소 1개 이상의 세션 정보가 필요합니다.")
    private List<SessionUploadRequest> sessionUploadRequests;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String status;
    @NotNull
    private Boolean isFree;
    @NotBlank
    private String location;
    @NotBlank
    private String mainImage;

    private String refundPolicy;
    private String schedule;
    private String dogSize;
    private String items;
    private String detailImage;
    private String difficulty;

    public static CourseBaseRequest from(CourseUploadRequest req) {
        if (req == null) return null;

        return CourseBaseRequest.builder()
                .type(req.getType())
                .lessonForm(req.getLessonForm())
                .sessionUploadRequests(req.getSessionUploadRequests())
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus())
                .isFree(req.getIsFree())
                .location(req.getLocation())
                .mainImage(req.getMainImage())
                .refundPolicy(req.getRefundPolicy())
                .schedule(req.getSchedule())
                .dogSize(req.getDogSize())
                .items(req.getItems())
                .detailImage(req.getDetailImage())
                .difficulty(req.getDifficulty())
                .build();
    }

    public static CourseBaseRequest from(CourseReuploadRequest req) {
        if (req == null) return null;

        return CourseBaseRequest.builder()
                .type(req.getType())
                .lessonForm(req.getLessonForm())
                .sessionUploadRequests(req.getSessionUploadRequests())
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus())
                .isFree(req.getIsFree())
                .location(req.getLocation())
                .mainImage(req.getMainImage())
                .refundPolicy(req.getRefundPolicy())
                .schedule(req.getSchedule())
                .dogSize(req.getDogSize())
                .items(req.getItems())
                .detailImage(req.getDetailImage())
                .difficulty(req.getDifficulty())
                .build();
    }
}