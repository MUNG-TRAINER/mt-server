package com.mungtrainer.mtserver.dog.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 반려견 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DogUpdateRequest {

    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요")
    private String name;

    @Size(max = 50, message = "견종은 50자 이내로 입력해주세요")
    private String breed;

    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 30, message = "나이는 30 이하여야 합니다")
    private Integer age;

    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F만 가능합니다")
    private String gender;

    private Boolean isNeutered;

    @DecimalMin(value = "0.0", message = "몸무게는 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "몸무게는 100 이하여야 합니다")
    private BigDecimal weight;

    @Size(max = 255, message = "성격은 255자 이내로 입력해주세요")
    private String personality;

    @Size(max = 255, message = "습관은 255자 이내로 입력해주세요")
    private String habits;

    @Size(max = 255, message = "건강 정보는 255자 이내로 입력해주세요")
    private String healthInfo;

    //추가: 프로필 이미지 URL (선택)
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이내로 입력해주세요")
    private String profileImageUrl;
}