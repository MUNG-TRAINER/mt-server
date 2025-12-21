package com.mungtrainer.mtserver.dog.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사회화 수준 Enum
 * 사람 및 동물에 대한 사회화 정도를 나타냄
 */
@Getter
@RequiredArgsConstructor
public enum SocializationLevel {

    /**
     * 낮음 - 사회화가 잘 되어 있지 않음
     */
    LOW("낮음"),

    /**
     * 보통 - 일반적인 사회화 수준
     */
    MEDIUM("보통"),

    /**
     * 높음 - 사회화가 매우 잘 되어 있음
     */
    HIGH("높음");

    private final String description;

    /**
     * JSON 직렬화 시 사용 (대문자로 출력)
     */
    @JsonValue
    public String toValue() {
        return this.name();
    }

    /**
     * JSON 역직렬화 시 대소문자 구분 없이 변환
     * "high", "High", "HIGH" 모두 허용
     */
    @JsonCreator
    public static SocializationLevel fromValue(String value) {
        if (value == null) {
            return null;
        }
        return SocializationLevel.valueOf(value.toUpperCase());
    }
}

