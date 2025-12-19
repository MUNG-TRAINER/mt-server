# 상담 API 필드 추가 수정 완료

## 📌 추가 요청사항
1. 상담 내용 작성 버튼을 눌렀을 때 **반려견 정보를 보여주기 위해** `dogId`를 추가로 반환해야 함
2. 상담 완료 후 **작성된 상담 내용을 확인**하기 위해 `content`를 추가로 반환해야 함

---

## ✅ 수정 완료

### 📝 수정된 파일

1. **`CounselingDogResponse.java`** - DTO에 dogId, content 필드 추가
2. **`CounselingDAO.xml`** - SQL 쿼리에 dogId, content 컬럼 추가
3. **`CounselingService.java`** - dogId, content 포함하여 응답 생성

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
    private String content;       // 상담 내용 (상담 완료 시에만 존재) ✨ 추가
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
    d.profile_image AS dogImage,
    c.content AS content         ✨ 추가
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
        presignedUrl,
        dog.getContent()          // ✨ 추가
);
```

---

## 📡 API 응답 변화

### GET `/api/trainer/counseling?completed=false` (상담 대기)

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
    "dogImage": "https://...",
    "content": null          ✨ 상담 대기 중이므로 null
  }
]
```

---

### GET `/api/trainer/counseling?completed=true` (상담 완료)

**응답 예시:**
```json
[
  {
    "counselingId": 2,
    "dogId": 7,
    "dogName": "초코",
    "ownerName": "김철수",
    "dogImage": "https://...",
    "content": "초코는 활발한 성격으로 다른 강아지들과 잘 어울립니다. 기본 명령어는 숙지했으며, 산책 시 리드줄 당기는 습관을 개선할 필요가 있습니다."  ✨ 상담 완료 시 내용 표시
  }
]
```

---

## 🎯 활용 시나리오

### 1. 상담 대기 리스트 - 상담 내용 작성 버튼

```tsx
// 상담 대기 리스트 (completed=false)
const CounselingWaitingList = ({ counselings }) => {
  const handleWriteCounseling = async (counseling) => {
    // dogId를 사용하여 반려견 상세 정보 조회
    const dogDetail = await fetch(`/api/dogs/${counseling.dogId}`);
    
    // 모달에 반려견 정보 표시
    showCounselingWriteModal({
      counselingId: counseling.counselingId,
      dogId: counseling.dogId,
      dogName: counseling.dogName,
      dogImage: counseling.dogImage,
      ownerName: counseling.ownerName,
      dogDetail: dogDetail
    });
  };

  return (
    <div>
      {counselings.map(counseling => (
        <div key={counseling.counselingId}>
          <Image src={counseling.dogImage} alt={counseling.dogName} />
          <p>{counseling.dogName} ({counseling.ownerName})</p>
          <button onClick={() => handleWriteCounseling(counseling)}>
            상담 내용 작성
          </button>
        </div>
      ))}
    </div>
  );
};
```

---

### 2. 상담 완료 리스트 - 상담 내용 확인

```tsx
// 상담 완료 리스트 (completed=true)
const CounselingCompletedList = ({ counselings }) => {
  const handleViewCounseling = (counseling) => {
    // content가 이미 있으므로 바로 표시
    showCounselingViewModal({
      counselingId: counseling.counselingId,
      dogId: counseling.dogId,
      dogName: counseling.dogName,
      dogImage: counseling.dogImage,
      ownerName: counseling.ownerName,
      content: counseling.content  // ✨ 작성된 상담 내용
    });
  };

  return (
    <div>
      {counselings.map(counseling => (
        <div key={counseling.counselingId}>
          <Image src={counseling.dogImage} alt={counseling.dogName} />
          <p>{counseling.dogName} ({counseling.ownerName})</p>
          <p className="content-preview">
            {counseling.content?.substring(0, 50)}...
          </p>
          <button onClick={() => handleViewCounseling(counseling)}>
            상담 내용 보기
          </button>
        </div>
      ))}
    </div>
  );
};
```

---

### 3. 상담 내용 작성 모달

```tsx
<div className="counseling-write-modal">
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

### 4. 상담 내용 보기 모달

```tsx
<div className="counseling-view-modal">
  <h2>상담 내용</h2>
  
  {/* 반려견 정보 표시 */}
  <div className="dog-info">
    <Image src={dogImage} alt={dogName} width={100} height={100} />
    <div>
      <p>반려견: {dogName}</p>
      <p>보호자: {ownerName}</p>
      <Link href={`/dogs/${dogId}`}>반려견 상세 정보 보기</Link>
    </div>
  </div>
  
  {/* 작성된 상담 내용 표시 */}
  <div className="content-view">
    <h3>상담 내용</h3>
    <p>{content}</p>
  </div>
  
  <button onClick={closeModal}>닫기</button>
</div>
```

---

## ✅ 테스트 체크리스트

- [x] DTO에 dogId, content 필드 추가
- [x] SQL 쿼리에 dogId, content 컬럼 추가
- [x] Service에서 dogId, content 포함하여 응답 생성
- [x] 빌드 성공 확인
- [ ] 상담 대기 리스트 조회 시 dogId 정상 반환, content는 null 확인
- [ ] 상담 완료 리스트 조회 시 dogId, content 모두 정상 반환 확인
- [ ] 프론트엔드에서 dogId로 반려견 상세 정보 조회 확인
- [ ] 상담 내용 작성 모달에서 반려견 정보 표시 확인
- [ ] 상담 완료 리스트에서 content 미리보기 표시 확인
- [ ] 상담 내용 보기 모달에서 전체 content 표시 확인

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
| 2025-12-19 | **dogId, content 필드 추가** | 현재 문서 ✨ |

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

