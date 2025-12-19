# 상담 API dogId 추가 수정 완료

## 📌 추가 요청사항
상담 내용 작성 버튼을 눌렀을 때 **반려견 정보를 보여주기 위해** `dogId`를 추가로 반환해야 함

---

## ✅ 수정 완료

### 📝 수정된 파일

1. **`CounselingDogResponse.java`** - DTO에 dogId 필드 추가
2. **`CounselingDAO.xml`** - SQL 쿼리에 dogId 컬럼 추가
3. **`CounselingService.java`** - dogId 포함하여 응답 생성

---

## 🔧 상세 수정 내용

### 1. DTO 수정 - `CounselingDogResponse.java`

**수정 전:**
```java
@Getter
@AllArgsConstructor
public class CounselingDogResponse {
    private Long counselingId;    // 상담 ID
    private String dogName;       // 반려견 이름
    private String ownerName;     // 보호자 이름
    private String dogImage;      // 반려견 프로필 이미지
}
```

**수정 후:**
```java
@Getter
@AllArgsConstructor
public class CounselingDogResponse {
    private Long counselingId;    // 상담 ID
    private Long dogId;           // 반려견 ID ✨ 추가
    private String dogName;       // 반려견 이름
    private String ownerName;     // 보호자 이름
    private String dogImage;      // 반려견 프로필 이미지
}
```

---

### 2. MyBatis Mapper XML 수정 - `CounselingDAO.xml`

**수정 전:**
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
</select>
```

**수정 후:**
```xml
<select id="findDogsByCompleted" resultType="...">
    SELECT
    c.counseling_id AS counselingId,
    d.dog_id AS dogId,           ✨ 추가
    d.name AS dogName,
    u.name AS ownerName,
    d.profile_image AS dogImage
    FROM counseling c
    JOIN dog d ON c.dog_id = d.dog_id
    JOIN user u ON d.user_id = u.user_id
    WHERE c.is_completed = CASE WHEN #{completed} = true THEN 1 ELSE 0 END
    AND c.is_deleted = 0
</select>
```

---

### 3. Service 수정 - `CounselingService.java`

**수정 전:**
```java
return new CounselingDogResponse(
        dog.getCounselingId(),
        dog.getDogName(),
        dog.getOwnerName(),
        presignedUrl
);
```

**수정 후:**
```java
return new CounselingDogResponse(
        dog.getCounselingId(),
        dog.getDogId(),           // ✨ 추가
        dog.getDogName(),
        dog.getOwnerName(),
        presignedUrl
);
```

---

## 📡 API 응답 변화

### GET `/api/trainer/counseling?completed=false`

**수정 전:**
```json
[
  {
    "counselingId": 1,
    "dogName": "뭉치",
    "ownerName": "홍길동",
    "dogImage": "https://..."
  }
]
```

**수정 후:**
```json
[
  {
    "counselingId": 1,
    "dogId": 5,              ✨ 추가
    "dogName": "뭉치",
    "ownerName": "홍길동",
    "dogImage": "https://..."
  }
]
```

---

## 🎯 활용 시나리오

### 프론트엔드 구현 예시

```tsx
// 상담 리스트에서 상담 내용 작성 버튼 클릭 시
const handleWriteCounselingContent = async (counseling) => {
  // dogId를 사용하여 반려견 상세 정보 조회
  const dogDetail = await fetch(`/api/dogs/${counseling.dogId}`);
  
  // 모달에 반려견 정보 표시
  showCounselingModal({
    counselingId: counseling.counselingId,
    dogId: counseling.dogId,
    dogName: counseling.dogName,
    dogImage: counseling.dogImage,
    ownerName: counseling.ownerName,
    dogDetail: dogDetail  // 추가 상세 정보 (품종, 나이 등)
  });
};
```

### 상담 내용 작성 화면 예시

```tsx
<div className="counseling-form">
  <h2>상담 내용 작성</h2>
  
  {/* 반려견 정보 표시 */}
  <div className="dog-info">
    <Image src={dogImage} alt={dogName} width={100} height={100} />
    <div>
      <p>반려견: {dogName}</p>
      <p>보호자: {ownerName}</p>
      <Link href={`/dogs/${dogId}`}>반려견 상세 정보 보기</Link>
    </div>
  </div>
  
  {/* 상담 내용 입력 */}
  <textarea 
    placeholder="상담 내용을 입력하세요" 
    value={content}
    onChange={(e) => setContent(e.target.value)}
  />
  
  <button onClick={() => submitCounseling(counselingId, content)}>
    상담 완료
  </button>
</div>
```

---

## ✅ 테스트 체크리스트

- [x] DTO에 dogId 필드 추가
- [x] SQL 쿼리에 dogId 컬럼 추가
- [x] Service에서 dogId 포함하여 응답 생성
- [x] 빌드 성공 확인
- [ ] API 호출 시 dogId 정상 반환 확인
- [ ] 프론트엔드에서 dogId로 반려견 상세 정보 조회 확인
- [ ] 상담 내용 작성 모달에서 반려견 정보 표시 확인

---

## 🔗 연관 API

### 반려견 상세 정보 조회
`dogId`를 받아서 반려견 상세 정보를 조회할 수 있습니다:

**API:** `GET /api/dogs/{dogId}`

**응답 예시:**
```json
{
  "dogId": 5,
  "name": "뭉치",
  "breed": "골든 리트리버",
  "age": 3,
  "gender": "M",
  "weight": 28.5,
  "profileImage": "https://...",
  "personality": "활발하고 사람을 좋아함",
  "healthInfo": "슬개골 탈구 주의"
}
```

---

## 📋 전체 수정 이력

| 날짜 | 내용 | 문서 |
|------|------|------|
| 2025-12-19 | 반려견 이미지 필드 추가 | `상담_API_반려견_이미지_추가_수정사항.md` |
| 2025-12-19 | Presigned URL 반환 수정 | `상담_API_Presigned_URL_수정_완료.md` |
| 2025-12-19 | **dogId 필드 추가** | 현재 문서 ✨ |

---

## ✅ 빌드 확인

```bash
./gradlew build -x test
```
**결과:** BUILD SUCCESSFUL ✅

---

**수정 일자:** 2025-12-19  
**수정자:** GitHub Copilot  
**상태:** ✅ 완료

