# 상담 API Presigned URL 반환 수정 완료

## ✅ 수정 완료 내역

### 🎯 문제점
상담 API(`GET /api/trainer/counseling`)에서 반려견 이미지를 **S3 키** 형태로 반환하여 프론트엔드에서 사용 불가능한 문제가 있었습니다.

**이전 응답:**
```json
{
  "counselingId": 1,
  "dogName": "뭉치",
  "ownerName": "홍길동",
  "dogImage": "dog-profile/1/dog2-1765940776168.jpeg"  ❌ S3 키
}
```

**Next.js 에러:**
```
Error: Failed to parse src "dog-profile/1/dog2-1765940776168.jpeg" 
on `next/image`, if using relative image it must start with a 
leading slash "/" or be an absolute URL (http:// or https://)
```

---

## 🔧 수정 내용

### 1. CounselingService.java 수정

**파일 경로:** `src/main/java/com/mungtrainer/mtserver/counseling/service/CounselingService.java`

#### 변경 사항

**1) Import 추가 및 S3Service 주입**
```java
import com.mungtrainer.mtserver.common.s3.S3Service;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselingService {
    private final CounselingDAO counselingDao;
    private final S3Service s3Service;  // ✨ S3Service 주입
    // ...
}
```

**2) getDogsByCompleted 메서드 수정**

**수정 전:**
```java
public List<CounselingDogResponse> getDogsByCompleted(boolean completed){
    return counselingDao.findDogsByCompleted(completed);
}
```

**수정 후:**
```java
public List<CounselingDogResponse> getDogsByCompleted(boolean completed) {
    List<CounselingDogResponse> dogs = counselingDao.findDogsByCompleted(completed);
    
    // S3 키를 Presigned URL로 변환
    return dogs.stream()
            .map(dog -> {
                String presignedUrl = null;
                if (dog.getDogImage() != null && !dog.getDogImage().isEmpty()) {
                    try {
                        presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getDogImage());
                        log.debug("Presigned URL 생성 완료 - key: {}", dog.getDogImage());
                    } catch (Exception e) {
                        log.error("Presigned URL 생성 실패 - key: {}, error: {}", dog.getDogImage(), e.getMessage());
                        // 실패해도 null로 처리하여 다른 데이터는 정상 반환
                    }
                }
                return new CounselingDogResponse(
                        dog.getCounselingId(),
                        dog.getDogName(),
                        dog.getOwnerName(),
                        presignedUrl
                );
            })
            .collect(Collectors.toList());
}
```

---

## 📡 수정 후 API 응답

### GET `/api/trainer/counseling?completed=false`

**현재 응답 (수정 후):**
```json
[
  {
    "counselingId": 1,
    "dogName": "뭉치",
    "ownerName": "홍길동",
    "dogImage": "https://your-bucket.s3.ap-northeast-2.amazonaws.com/dog-profile/1/dog2-1765940776168.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."  ✅ Presigned URL
  },
  {
    "counselingId": 2,
    "dogName": "초코",
    "ownerName": "김철수",
    "dogImage": "https://your-bucket.s3.ap-northeast-2.amazonaws.com/dog-profile/2/dog3-1765940776169.jpeg?X-Amz-Algorithm=..."  ✅ Presigned URL
  }
]
```

### 이미지가 없는 경우
```json
{
  "counselingId": 3,
  "dogName": "콩이",
  "ownerName": "이영희",
  "dogImage": null  // 이미지가 없으면 null 반환
}
```

---

## 🎯 주요 개선사항

### ✅ 안전한 에러 처리
- Presigned URL 생성 실패 시에도 다른 데이터는 정상 반환
- 에러 발생 시 로그 기록으로 문제 추적 가능

### ✅ NULL 안전성
- 이미지가 없거나 빈 문자열인 경우 안전하게 처리
- NPE(NullPointerException) 방지

### ✅ 성능 고려
- Stream API를 사용한 함수형 프로그래밍 스타일
- 필요한 경우 `parallelStream()`으로 병렬 처리 가능

---

## 🔍 다른 API 확인 결과

### ✅ 이미 Presigned URL 적용된 API들

다음 API들은 이미 Presigned URL을 올바르게 반환하고 있습니다:

#### 1. 반려견 API (DogService)
- `GET /api/dogs` - 본인 반려견 목록 ✅
- `GET /api/dogs/{dogId}` - 반려견 상세 ✅
- `GET /api/users/{username}/dogs` - 타인 반려견 목록 ✅

**적용 코드:**
```java
private void convertProfileImageToPresignedUrl(DogResponse dog) {
    if (dog.getProfileImage() != null && !dog.getProfileImage().isBlank()) {
        String presignedUrl = s3Service.generateDownloadPresignedUrl(dog.getProfileImage());
        dog.setProfileImage(presignedUrl);
    }
}

private void convertProfileImagesToPresignedUrls(List<DogResponse> dogs) {
    dogs.forEach(this::convertProfileImageToPresignedUrl);
}
```

#### 2. 훈련사 API (TrainerService)
- `GET /api/trainer/{trainerId}` - 훈련사 프로필 ✅

**적용 코드:**
```java
String profileFileKey = user.getProfileImage();
String profilePresignedUrl = null;

if(profileFileKey != null && !profileFileKey.isBlank()) {
    profilePresignedUrl = s3Service.generateDownloadPresignedUrl(profileFileKey);
}

return TrainerResponse.builder()
        .profileImage(profilePresignedUrl)
        // ...
        .build();
```

---

## 📝 Presigned URL 설정 정보

### 만료 시간
- **현재 설정**: `application.yml`의 `aws.s3.presigned-url-expiration-minutes` 값
- **권장 값**: 60분 (1시간)
- **최대 값**: 7일

### 설정 확인
```yaml
# application.yml
aws:
  s3:
    bucket: your-bucket-name
    presigned-url-expiration-minutes: 60  # 1시간
```

---

## 🧪 테스트 체크리스트

- [x] 빌드 성공 확인
- [ ] 상담 대기 리스트 조회 시 Presigned URL 정상 반환 확인
- [ ] 상담 완료 리스트 조회 시 Presigned URL 정상 반환 확인
- [ ] 이미지가 없는 반려견의 경우 null 반환 확인
- [ ] Presigned URL이 실제로 이미지를 다운로드할 수 있는지 확인
- [ ] Presigned URL 만료 시간 확인
- [ ] 프론트엔드에서 Next.js Image 컴포넌트로 정상 렌더링 확인
- [ ] 에러 로그 확인 (Presigned URL 생성 실패 케이스)

---

## 🚀 배포 전 확인사항

### 1. 환경 변수 확인
```bash
# AWS S3 설정이 올바른지 확인
aws.s3.bucket=your-bucket-name
aws.s3.region=ap-northeast-2
aws.s3.presigned-url-expiration-minutes=60
```

### 2. AWS IAM 권한 확인
S3 Presigned URL 생성을 위한 IAM 권한 필요:
```json
{
  "Effect": "Allow",
  "Action": [
    "s3:GetObject",
    "s3:PutObject"
  ],
  "Resource": "arn:aws:s3:::your-bucket-name/*"
}
```

### 3. 로그 레벨 설정
Presigned URL 생성 로그를 확인하려면:
```yaml
logging:
  level:
    com.mungtrainer.mtserver.counseling.service: DEBUG
```

---

## 📞 프론트엔드 연동 가이드

### Next.js Image 컴포넌트 사용 예시

**수정 전 (임시방편):**
```tsx
<Image
  src={`https://your-bucket.s3.ap-northeast-2.amazonaws.com/${dogImage}`}
  alt={dogName}
  width={100}
  height={100}
/>
```

**수정 후 (백엔드에서 Presigned URL 반환):**
```tsx
<Image
  src={dogImage}  // 이미 완전한 URL이므로 그대로 사용
  alt={dogName}
  width={100}
  height={100}
/>
```

### NULL 처리
```tsx
{dogImage ? (
  <Image src={dogImage} alt={dogName} width={100} height={100} />
) : (
  <div className="default-avatar">기본 이미지</div>
)}
```

---

## 🎉 완료

- ✅ CounselingService에서 Presigned URL 생성 로직 추가
- ✅ S3Service 주입 및 활용
- ✅ 안전한 에러 처리 구현
- ✅ NULL 안전성 확보
- ✅ 빌드 성공 확인
- ✅ 다른 API들도 이미 Presigned URL 적용 확인

---

**수정 일자:** 2025-12-19  
**수정자:** GitHub Copilot  
**긴급도:** ⚠️ 높음 (프로덕션 에러 해결)  
**상태:** ✅ 완료

