package com.mungtrainer.mtserver.dog.mapper;

import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 반려견 정보 매퍼 인터페이스
 */
@Mapper
public interface DogMapper {

    /**
     * 반려견 정보 생성
     * @param params userId, request, createdBy, updatedBy, dogId를 포함한 맵
     * @return 생성된 행 수
     */
    int insertDog(Map<String, Object> params);

    /**
     * 반려견 정보 조회 (단건)
     * @param dogId 반려견 ID
     * @return 반려견 정보
     */
    DogResponse selectDogById(@Param("dogId") Long dogId);

    /**
     * 반려견 정보 조회 (소유자 확인)
     * @param dogId 반려견 ID
     * @param userId 사용자 ID
     * @return 반려견 정보
     */
    DogResponse selectDogByIdAndUserId(@Param("dogId") Long dogId,
                                       @Param("userId") Long userId);

    /**
     * 반려견 리스트 조회 (본인)
     * @param userId 사용자 ID
     * @return 반려견 리스트
     */
    List<DogResponse> selectDogsByUserId(@Param("userId") Long userId);

    /**
     * 반려견 리스트 조회 (타인)
     * @param username 사용자명
     * @return 반려견 리스트
     */
    List<DogResponse> selectDogsByUsername(@Param("username") String username);

    /**
     * 반려견 정보 수정
     * @param dogId 반려견 ID
     * @param userId 사용자 ID (권한 확인용)
     * @param request 수정 요청 정보
     * @param updatedBy 수정자 ID
     * @return 수정된 행 수
     */
    int updateDog(@Param("dogId") Long dogId,
                  @Param("userId") Long userId,
                  @Param("request") DogUpdateRequest request,
                  @Param("updatedBy") Long updatedBy);

    /**
     * 프로필 이미지 업데이트
     * @param dogId 반려견 ID
     * @param profileImage 프로필 이미지 URL
     * @param updatedBy 수정자 ID
     * @return 수정된 행 수
     */
    int updateProfileImage(@Param("dogId") Long dogId,
                          @Param("profileImage") String profileImage,
                          @Param("updatedBy") Long updatedBy);

    /**
     * 반려견 정보 삭제 (소프트 삭제)
     * @param dogId 반려견 ID
     * @param userId 사용자 ID (권한 확인용)
     * @param deletedBy 삭제자 ID
     * @return 삭제된 행 수
     */
    int deleteDog(@Param("dogId") Long dogId,
                  @Param("userId") Long userId,
                  @Param("deletedBy") Long deletedBy);

    /**
     * 반려견 이름 중복 확인
     * @param userId 사용자 ID
     * @param name 반려견 이름
     * @return 중복 여부
     */
    boolean existsByUserIdAndName(@Param("userId") Long userId,
                                  @Param("name") String name);

    /**
     * 마지막으로 생성된 반려견 ID 조회
     * @return 마지막 생성 ID
     */
    Long selectLastInsertId();
}