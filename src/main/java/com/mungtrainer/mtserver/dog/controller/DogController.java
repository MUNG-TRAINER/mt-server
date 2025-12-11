package com.mungtrainer.mtserver.dog.controller;

    import com.mungtrainer.mtserver.dog.dto.request.DogCreateRequest;
    import com.mungtrainer.mtserver.dog.dto.request.DogImageUploadRequest;
    import com.mungtrainer.mtserver.dog.dto.request.DogUpdateRequest;
    import com.mungtrainer.mtserver.dog.dto.response.DogImageUploadResponse;
    import com.mungtrainer.mtserver.dog.dto.response.DogResponse;
    import com.mungtrainer.mtserver.dog.service.DogService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

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

        // TODO: 인증 구현 후 제거 - 임시로 더미 데이터의 user_id=2(수강자) 사용
        private static final Long TEMP_USER_ID = 2L;

        /**
         * 반려견 등록
         * @param request 반려견 생성 요청 (프로필 이미지 URL 포함)
         * @return 생성된 반려견 ID
         */
        @PostMapping("/dogs")
        public ResponseEntity<Long> createDog(@Valid @RequestBody DogCreateRequest request) {
            log.info("반려견 등록 API 호출 - userId: {}", TEMP_USER_ID);
            Long dogId = dogService.createDog(TEMP_USER_ID, request);
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
         * 본인의 반려견 리스트 조회 (임시: userId=2 고정)
         * @return 반려견 리스트
         */
        @GetMapping("/dogs")
        public ResponseEntity<List<DogResponse>> getMyDogs() {
            log.info("본인 반려견 리스트 조회 API 호출 - userId: {}", TEMP_USER_ID);
            List<DogResponse> dogs = dogService.getMyDogs(TEMP_USER_ID);
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
         * @param dogId 반려견 ID
         * @param request 수정 요청 (프로필 이미지 URL 포함)
         * @return 수정 완료 응답
         */
        @PatchMapping("/dogs/{dogId}")
        public ResponseEntity<Void> updateDog(
                @PathVariable Long dogId,
                @Valid @RequestBody DogUpdateRequest request) {

            log.info("반려견 정보 수정 API 호출 - userId: {}, dogId: {}", TEMP_USER_ID, dogId);
            dogService.updateDog(TEMP_USER_ID, dogId, request);
            return ResponseEntity.ok().build();
        }

        /**
         * 반려견 정보 삭제
         * @param dogId 반려견 ID
         * @return 삭제 완료 응답
         */
        @DeleteMapping("/dogs/{dogId}")
        public ResponseEntity<Void> deleteDog(@PathVariable Long dogId) {
            log.info("반려견 삭제 API 호출 - userId: {}, dogId: {}", TEMP_USER_ID, dogId);
            dogService.deleteDog(TEMP_USER_ID, dogId);
            return ResponseEntity.noContent().build();
        }

        /**
         * 프로필 이미지 업로드용 Presigned URL 발급 (신규 등록용)
         * @param request 파일 키 및 메타정보
         * @return 업로드 URL
         */
        @PostMapping("/dogs/upload-url")
        public ResponseEntity<DogImageUploadResponse> generateUploadUrl(
                @Valid @RequestBody DogImageUploadRequest request) {

            log.info("프로필 이미지 업로드 URL 발급 API 호출 (신규 등록) - userId: {}, fileKey: {}",
                     TEMP_USER_ID, request.getFileKey());
            DogImageUploadResponse response = dogService.generateUploadUrl(TEMP_USER_ID, null, request);
            return ResponseEntity.ok(response);
        }

        /**
         * 프로필 이미지 업로드용 Presigned URL 발급 (수정용)
         * @param dogId 반려견 ID
         * @param request 파일 키 및 메타정보
         * @return 업로드 URL
         */
        @PostMapping("/dogs/{dogId}/upload-url")
        public ResponseEntity<DogImageUploadResponse> generateUploadUrlForUpdate(
                @PathVariable Long dogId,
                @Valid @RequestBody DogImageUploadRequest request) {

            log.info("프로필 이미지 업로드 URL 발급 API 호출 (수정) - userId: {}, dogId: {}, fileKey: {}",
                     TEMP_USER_ID, dogId, request.getFileKey());
            DogImageUploadResponse response = dogService.generateUploadUrl(TEMP_USER_ID, dogId, request);
            return ResponseEntity.ok(response);
        }
    }