package com.mungtrainer.mtserver.common.util;

import java.util.regex.Pattern;

/**
 * 전화번호 검증 유틸리티 클래스
 */
public class PhoneValidator {

    /**
     * 한국 전화번호 정규식 패턴
     * 형식: 02-123-4567, 010-1234-5678, 031-123-4567 등
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{2,3}-\\d{3,4}-\\d{4}$");

    /**
     * 전화번호 형식이 유효한지 검증
     *
     * @param phone 검증할 전화번호
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValid(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 전화번호를 표준 형식으로 포맷팅
     * 숫자만 추출하여 하이픈(-) 추가
     *
     * @param phone 포맷팅할 전화번호
     * @return 포맷팅된 전화번호 (예: 01012345678 -> 010-1234-5678)
     */
    public static String format(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        // 숫자만 추출
        String numbers = phone.replaceAll("[^0-9]", "");

        // 10자리 (지역번호 2자리)
        if (numbers.length() == 10) {
            return numbers.replaceFirst("(\\d{2})(\\d{3,4})(\\d{4})", "$1-$2-$3");
        }
        // 11자리 (휴대폰)
        else if (numbers.length() == 11) {
            return numbers.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        // 9자리 (서울 02)
        else if (numbers.length() == 9 && numbers.startsWith("02")) {
            return numbers.replaceFirst("(\\d{2})(\\d{3})(\\d{4})", "$1-$2-$3");
        }

        // 형식에 맞지 않으면 원본 반환
        return phone;
    }

    /**
     * Private 생성자 - 인스턴스화 방지
     */
    private PhoneValidator() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}

