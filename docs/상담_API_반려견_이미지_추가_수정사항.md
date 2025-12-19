# 상담 API 반려견 이미지 추가 수정사항

## 📌 개요
훈련사의 상담 완료 전/후 리스트 조회 API에서 반려견 프로필 이미지를 추가로 반환하도록 수정했습니다.

---

## 🔧 수정 내용

### 1. DTO 수정 - `CounselingDogResponse.java`

**파일 경로:** `src/main/java/com/mungtrainer/mtserver/counseling/dto/response/CounselingDogResponse.java`

#### 수정 전
```java
@Getter
@AllArgsConstructor
public class CounselingDogResponse {
    private Long counselingId;    // 상담 ID
    private String dogName;       // 반려견 이름
    private String ownerName;     // 보호자 이름
}
```

#### 수정 후
```java
@Getter
@AllArgsConstructor
public class CounselingDogResponse {
    private Long counselingId;    // 상담 ID
    private String dogName;       // 반려견 이름
    private String ownerName;     // 보호자 이름
    private String dogImage;      // 반려견 프로필 이미지 ✨ 추가
}
```

**변경사항:**
- `dogImage` 필드를 추가하여 반려견 프로필 이미지 URL을 응답에 포함

---

### 2. MyBatis Mapper XML 수정 - `CounselingDAO.xml`

**파일 경로:** `src/main/resources/mapper/counseling/CounselingDAO.xml`

#### 수정 전
```xml
<!-- 훈련사 상담 완료 전후 반려견 리스트 조회 -->
<select id="findDogsByCompleted" resultType="com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponse">
    SELECT
    c.counseling_id AS counselingId,
    d.name AS dogName,
    u.name AS ownerName
    FROM counseling c
    JOIN dog d ON c.dog_id = d.dog_id
    JOIN user u ON d.user_id = u.user_id
    WHERE c.is_completed = CASE WHEN #{completed} = true THEN 1 ELSE 0 END
    AND c.is_deleted = 0
</select>
```

#### 수정 후
```xml
<!-- 훈련사 상담 완료 전후 반려견 리스트 조회 -->
<select id="findDogsByCompleted" resultType="com.mungtrainer.mtserver.counseling.dto.response.CounselingDogResponse">
    SELECT
    c.counseling_id AS counselingId,
    d.name AS dogName,
    u.name AS ownerName,
    d.profile_image AS dogImage    ✨ 추가
    FROM counseling c
    JOIN dog d ON c.dog_id = d.dog_id
    JOIN user u ON d.user_id = u.user_id
    WHERE c.is_completed = CASE WHEN #{completed} = true THEN 1 ELSE 0 END
    AND c.is_deleted = 0
</select>
```

**변경사항:**
- SELECT 절에 `d.profile_image AS dogImage` 컬럼을 추가하여 반려견 테이블의 프로필 이미지를 조회

---

## 📡 API 엔드포인트

### GET `/api/trainer/counseling`

**설명:** 훈련사의 상담 완료 전/후 반려견 리스트 조회

**Controller:** `CounselingTrainerController.java`

**요청 파라미터:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| completed | boolean | O | true: 상담 완료, false: 상담 대기 |

**Controller 코드:**
```java
@GetMapping("/counseling")
public List<CounselingDogResponse> getCounselingDogs(
        @RequestParam boolean completed
) {
    return counselingService.getDogsByCompleted(completed);
}
```

**응답 예시:**

#### 상담 대기 리스트 조회
```http
GET /api/trainer/counseling?completed=false
```

```json
[
  {
    "counselingId": 1,
    "dogName": "뭉치",
    "ownerName": "홍길동",
    "dogImage": "https://s3.amazonaws.com/bucket/dog-profile/123.jpg"
  },
  {
    "counselingId": 2,
    "dogName": "초코",
    "ownerName": "김철수",
    "dogImage": "https://s3.amazonaws.com/bucket/dog-profile/456.jpg"
  }
]
```

#### 상담 완료 리스트 조회
```http
GET /api/trainer/counseling?completed=true
```

```json
[
  {
    "counselingId": 3,
    "dogName": "콩이",
    "ownerName": "이영희",
    "dogImage": "https://s3.amazonaws.com/bucket/dog-profile/789.jpg"
  }
]
```

---

## 🗄️ 데이터베이스 구조

### 관련 테이블

#### counseling 테이블
```sql
counseling_id (PK)
dog_id (FK)
phone
content
is_completed
is_deleted
created_by
updated_by
created_at
updated_at
deleted_at
```

#### dog 테이블
```sql
dog_id (PK)
user_id (FK)
name
breed
age
gender
profile_image  ← 이미지 필드
...
```

#### user 테이블
```sql
user_id (PK)
name
email
...
```

### 테이블 관계도
```
counseling (N) → (1) dog (N) → (1) user
```

---

## ✅ 테스트 체크리스트

- [ ] 상담 대기 리스트 조회 시 반려견 이미지가 정상적으로 반환되는지 확인
- [ ] 상담 완료 리스트 조회 시 반려견 이미지가 정상적으로 반환되는지 확인
- [ ] 반려견 프로필 이미지가 NULL인 경우 응답 확인
- [ ] S3 이미지 URL이 유효한지 확인
- [ ] 다수의 상담 건이 있을 때 성능 확인 (JOIN 최적화)
- [ ] API 응답 시간 측정 (권장: 1초 이내)

---

## 🔍 코드 리뷰 포인트

### ✅ 좋은 점
1. **명확한 필드명**: `dogImage`로 반려견 이미지임을 명확하게 표현
2. **JOIN 활용**: N+1 문제 없이 한 번의 쿼리로 필요한 데이터 조회
3. **일관된 응답 형식**: 기존 필드와 일관성 있게 camelCase 사용
4. **불변 객체**: `@AllArgsConstructor`와 `@Getter`를 사용한 불변 DTO 구조

### 💡 개선 제안사항

#### 1. NULL 처리
반려견 이미지가 없는 경우를 대비한 기본 이미지 처리 고려:

```java
@Getter
public class CounselingDogResponse {
    private Long counselingId;
    private String dogName;
    private String ownerName;
    private String dogImage;
    
    public CounselingDogResponse(Long counselingId, String dogName, 
                                  String ownerName, String dogImage) {
        this.counselingId = counselingId;
        this.dogName = dogName;
        this.ownerName = ownerName;
        // 이미지가 없으면 기본 이미지 URL 설정
        this.dogImage = (dogImage != null && !dogImage.isEmpty()) 
            ? dogImage 
            : "/images/default-dog-profile.png";
    }
}
```

#### 2. 인덱스 최적화
상담 조회 성능 향상을 위한 복합 인덱스 추가 고려:

```sql
-- 상담 완료 여부별 조회 최적화
CREATE INDEX idx_counseling_completed_deleted 
ON counseling (is_completed, is_deleted);

-- 반려견-사용자 조인 최적화
CREATE INDEX idx_dog_user_id 
ON dog (user_id);
```

#### 3. 정렬 기준 추가
상담 신청 순서를 명확하게 하기 위한 정렬 추가:

```xml
<select id="findDogsByCompleted" resultType="...">
    SELECT
    c.counseling_id AS counselingId,
    d.name AS dogName,
    u.name AS ownerName,
    d.profile_image AS dogImage
    FROM counseling c
    JOIN dog d ON c.dog_id = d.dog_id
    JOIN user u ON d.user_id = u.user_id
    WHERE c.is_completed = CASE WHEN #{completed} = true THEN 1 ELSE 0 END
    AND c.is_deleted = 0
    ORDER BY c.created_at DESC  -- 최신 순 정렬 추가
    LIMIT 100  -- 대량 데이터 방지
</select>
```

#### 4. 페이징 처리
대량 데이터 처리를 위한 페이징 적용 고려:

```java
@GetMapping("/counseling")
public ResponseEntity<Page<CounselingDogResponse>> getCounselingDogs(
        @RequestParam boolean completed,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    Page<CounselingDogResponse> result = 
        counselingService.getDogsByCompleted(completed, page, size);
    return ResponseEntity.ok(result);
}
```

---

## 📝 추가 참고사항

### S3 Presigned URL 사용 시
만약 이미지가 S3에 저장되어 있고 Presigned URL이 필요한 경우:

1. **Service 레이어에서 S3 URL 변환 로직 추가**
```java
@Service
@RequiredArgsConstructor
public class CounselingService {
    private final S3Service s3Service;
    
    public List<CounselingDogResponse> getDogsByCompleted(boolean completed) {
        List<CounselingDogResponse> list = counselingDao.findDogsByCompleted(completed);
        
        // S3 Presigned URL로 변환
        return list.stream()
            .map(response -> new CounselingDogResponse(
                response.getCounselingId(),
                response.getDogName(),
                response.getOwnerName(),
                s3Service.generatePresignedUrl(response.getDogImage(), 3600) // 1시간 유효
            ))
            .collect(Collectors.toList());
    }
}
```

2. **만료 시간 적절히 설정** (권장: 1시간)
3. **캐싱 전략 고려** (동일 이미지 중복 요청 방지)

### 보안 고려사항
- ✅ 훈련사가 자신이 담당하는 상담 건만 조회할 수 있도록 권한 체크 필요
- ✅ 민감한 개인정보가 로그에 남지 않도록 주의
- ✅ SQL Injection 방지를 위해 `#{}` 파라미터 바인딩 사용 (현재 적용됨)

### 성능 최적화
- **쿼리 실행 계획 확인**: `EXPLAIN` 명령어로 인덱스 사용 여부 확인
- **커넥션 풀 모니터링**: 동시 요청 시 커넥션 부족 여부 확인
- **응답 캐싱**: Redis 등을 활용한 자주 조회되는 데이터 캐싱 고려

---

## 🚀 배포 체크리스트

### 배포 전
- [ ] 로컬 환경에서 테스트 완료
- [ ] 단위 테스트 작성 및 통과
- [ ] 코드 리뷰 완료
- [ ] API 문서 업데이트 (Swagger 등)

### 배포 후
- [ ] API 응답 정상 확인
- [ ] 에러 로그 모니터링
- [ ] 성능 모니터링 (응답 시간, DB 쿼리 시간)
- [ ] 프론트엔드 연동 테스트

---

## 📅 수정 이력
| 날짜 | 수정자 | 내용 |
|------|--------|------|
| 2025-12-19 | GitHub Copilot | 반려견 이미지 필드 추가 |

---

## 📞 문의
수정사항에 대한 문의사항이 있으시면 개발팀에 연락 주세요.

---

**수정 완료 ✅**

