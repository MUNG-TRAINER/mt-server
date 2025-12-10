package com.mungtrainer.mtserver.dog.controller;

import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
import com.mungtrainer.mtserver.dog.service.DogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 반려견 정보 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DogController {

    private final DogService dogService;

    /**
     * 반려견 등록
     * @param userId 사용자 ID (인증 후 SecurityContext에서 가져올 예정)
     * @param request 반려견 생성 요청
     * @param profileImage 프로필 이미지 (선택)
     * @return 생성된 반려견 ID
     */
    @PostMapping(value = "/dogs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createDog(
            // TODO: @AuthenticationPrincipal 또는 SecurityContext에서 userId 가져오기
            @RequestParam Long userId,
            @Valid @RequestPart("dog") DogCreateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("반려견 등록 API 호출 - userId: {}", userId);
        Long dogId = dogService.createDog(userId, request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(dogId);
    }

    /**
     * 반려견 정보 조회
     * @param dogId 반려견 ID
     * @return 반려견 정보
     */
    @GetMapping("/dogs/{dogId}")
    public ResponseEntity<DogResponse> getDog(@PathVariable Long dogId) {
        log.info("반려견 조회 API 호출 - dogId: {}", dogId);
        DogResponse dog = dogService.getDog(dogId);
        return ResponseEntity.ok(dog);
    }

    /**
     * 본인의 반려견 리스트 조회
     * @param userId 사용자 ID (인증 후 SecurityContext에서 가져올 예정)
     * @return 반려견 리스트
     */
    @GetMapping("/dogs")
    public ResponseEntity<List<DogResponse>> getMyDogs(
            // TODO: @AuthenticationPrincipal 또는 SecurityContext에서 userId 가져오기
            @RequestParam Long userId) {

        log.info("본인 반려견 리스트 조회 API 호출 - userId: {}", userId);
        List<DogResponse> dogs = dogService.getMyDogs(userId);
        return ResponseEntity.ok(dogs);
    }

    /**
     * 타인의 반려견 리스트 조회
     * @param username 사용자명
     * @return 반려견 리스트
     */
    @GetMapping("/users/{username}/dogs")
    public ResponseEntity<List<DogResponse>> getUserDogs(@PathVariable String username) {
        log.info("타인 반려견 리스트 조회 API 호출 - username: {}", username);
        List<DogResponse> dogs = dogService.getUserDogs(username);
        return ResponseEntity.ok(dogs);
    }

    /**
     * 반려견 정보 수정
     * @param userId 사용자 ID (인증 후 SecurityContext에서 가져올 예정)
     * @param dogId 반려견 ID
     * @param request 수정 요청
     * @param profileImage 프로필 이미지 (선택)
     * @return 수정 완료 응답
     */
    @PatchMapping(value = "/dogs/{dogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateDog(
            // TODO: @AuthenticationPrincipal 또는 SecurityContext에서 userId 가져오기
            @RequestParam Long userId,
            @PathVariable Long dogId,
            @Valid @RequestPart("dog") DogUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("반려견 정보 수정 API 호출 - userId: {}, dogId: {}", userId, dogId);
        dogService.updateDog(userId, dogId, request, profileImage);
        return ResponseEntity.ok().build();
    }

    /**
     * 반려견 정보 삭제
     * @param userId 사용자 ID (인증 후 SecurityContext에서 가져올 예정)
     * @param dogId 반려견 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/dogs/{dogId}")
    public ResponseEntity<Void> deleteDog(
            // TODO: @AuthenticationPrincipal 또는 SecurityContext에서 userId 가져오기
            @RequestParam Long userId,
            @PathVariable Long dogId) {

        log.info("반려견 삭제 API 호출 - userId: {}, dogId: {}", userId, dogId);
        dogService.deleteDog(userId, dogId);
        return ResponseEntity.noContent().build();
    }
}