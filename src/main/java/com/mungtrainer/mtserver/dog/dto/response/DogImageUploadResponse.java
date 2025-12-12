package com.mungtrainer.mtserver.dog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 반려견 프로필 이미지 업로드 URL 발급 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogImageUploadResponse {

    /**
     * S3 업로드용 Presigned URL
     */
    private String uploadUrl;

}