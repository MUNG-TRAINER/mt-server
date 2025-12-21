package com.mungtrainer.mtserver.dog.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mungtrainer.mtserver.dog.entity.SocializationLevel;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 반려견 정보 수정 요청 DTO
 *
 * 모든 필드는 선택값입니다. (부분 수정 지원)
 * 입력한 필드만 업데이트되며, 입력하지 않은 필드는 기존 값을 유지합니다.
 * 단, 값을 입력할 경우 유효성 검증이 적용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DogUpdateRequest {

    /**
     * 반려견 이름 (선택, 입력 시 50자 이내)
     * DB: NOT NULL이므로 빈 문자열("")은 업데이트되지 않음
     */
    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요")
    private String name;

    /**
     * 견종 (선택, 입력 시 50자 이내)
     * DB: NOT NULL이므로 빈 문자열("")은 업데이트되지 않음
     */
    @Size(max = 50, message = "견종은 50자 이내로 입력해주세요")
    private String breed;

    /**
     * 나이 (선택, 입력 시 0~30 사이)
     * DB: NOT NULL이므로 null은 업데이트되지 않음
     */
    @Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @Max(value = 30, message = "나이는 30 이하여야 합니다")
    private Integer age;

    /**
     * 성별 (선택, 입력 시 M 또는 F만 가능)
     * DB: NOT NULL이므로 빈 문자열("")은 업데이트되지 않음
     */
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F만 가능합니다")
    private String gender;

    /**
     * 중성화 여부 (선택)
     * DB: NOT NULL이므로 null은 업데이트되지 않음
     */
    private Boolean isNeutered;

    /**
     * 몸무게 (선택, 입력 시 0~100 사이)
     * DB: NULL 허용
     * 삭제 방법: 필드를 보내지 않거나(부분 수정), 빈 값 전달 불가 (숫자 타입)
     */
    @DecimalMin(value = "0.0", message = "몸무게는 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "몸무게는 100 이하여야 합니다")
    private BigDecimal weight;

    /**
     * 성격 (선택, 입력 시 255자 이내)
     * DB: NULL 허용
     * 삭제 방법: 빈 문자열("") 전송 → DB에 NULL로 저장됨 (필드를 보내지 않으면 기존 값 유지)
     */
    @Size(max = 255, message = "성격은 255자 이내로 입력해주세요")
    private String personality;

    /**
     * 습관 (선택, 입력 시 255자 이내)
     * DB: NULL 허용
     * 삭제 방법: 빈 문자열("") 전송 → DB에 NULL로 저장됨 (필드를 보내지 않으면 기존 값 유지)
     */
    @Size(max = 255, message = "습관은 255자 이내로 입력해주세요")
    private String habits;

    /**
     * 건강 정보 (선택, 입력 시 255자 이내)
     * DB: NULL 허용
     * 삭제 방법: 빈 문자열("") 전송 → DB에 NULL로 저장됨 (필드를 보내지 않으면 기존 값 유지)
     */
    @Size(max = 255, message = "건강 정보는 255자 이내로 입력해주세요")
    private String healthInfo;

    /**
     * 사람 사회화 수준 (선택, LOW/MEDIUM/HIGH)
     * DB: NOT NULL이므로 null은 업데이트되지 않음
     */
    private SocializationLevel humanSocialization;

    /**
     * 동물 사회화 수준 (선택, LOW/MEDIUM/HIGH)
     * DB: NOT NULL이므로 null은 업데이트되지 않음
     */
    private SocializationLevel animalSocialization;

    /**
     * 프로필 이미지 URL (선택, 입력 시 500자 이내)
     * DB: NULL 허용
     * 삭제 방법: 빈 문자열("") 전송 → DB에 NULL로 저장됨 (필드를 보내지 않으면 기존 값 유지)
     */
    @JsonProperty("profileImage")
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이내로 입력해주세요")
    private String profileImageUrl;

    /**
     * 체중 삭제 플래그 (선택)
     * true: 체중을 NULL로 설정
     */
    @JsonProperty("clearWeight")
    private Boolean clearWeight;
}