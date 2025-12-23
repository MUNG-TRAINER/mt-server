# 🐛 버그 수정: 단회차 훈련 출석 상태 누락

## 📅 수정일: 2025-12-23

---

## 🚨 문제 발견

### 프론트엔드 로그
```javascript
trainingApplications.map((training) => {
  console.log("단회차 출석 상태:", {
    sessionId: training.sessionId,
    courseTitle: training.courseTitle,
    attendanceStatus: training.attendanceStatus,  // ❌ undefined
    type: typeof training.attendanceStatus,        // ❌ "undefined"
  });
});
```

**문제**: `attendanceStatus`가 `undefined`로 표시됨

---

## 🔍 원인 분석

### 1. DTO 확인
```java
// TrainingApplicationResponse.java (MyBatis에서 조회)
private String attendanceStatus;  // ✅ 필드 있음
```

### 2. Service 확인
```java
// TrainerUserService.java
List<DogStatsResponse.TrainingSessionDto> simplified =
    singleApps.stream()
        .map(item -> DogStatsResponse.TrainingSessionDto.builder()
            .courseId(item.getCourseId())
            .courseTitle(item.getCourseTitle())
            // ...
            // ❌ attendanceStatus 매핑 누락!
            .build()
        ).toList();
```

### 3. 응답 DTO 확인
```java
// DogStatsResponse.TrainingSessionDto
public static class TrainingSessionDto {
    private Long courseId;
    private String courseTitle;
    // ...
    // ❌ attendanceStatus 필드 없음!
}
```

**결론**: DTO에 필드가 없고, Service에서 매핑도 안 함!

---

## ✅ 수정 내용

### 1. DogStatsResponse.java 수정
```java
@Getter @Setter @ToString @Builder
public static class TrainingSessionDto {
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String tags;
    private String type;
    private Long sessionId;
    private LocalDate sessionDate;
    private LocalTime sessionStartTime;
    private LocalTime sessionEndTime;
    private String attendanceStatus;  // ⭐ 추가
}
```

### 2. TrainerUserService.java 수정
```java
List<DogStatsResponse.TrainingSessionDto> simplified =
    singleApps.stream()
        .map(item -> DogStatsResponse.TrainingSessionDto.builder()
            .courseId(item.getCourseId())
            .courseTitle(item.getCourseTitle())
            .courseDescription(item.getCourseDescription())
            .tags(item.getTags())
            .type(item.getType())
            .sessionId(item.getSessionId())
            .sessionDate(item.getSessionDate())
            .sessionStartTime(item.getSessionStartTime())
            .sessionEndTime(item.getSessionEndTime())
            .attendanceStatus(item.getAttendanceStatus())  // ⭐ 매핑 추가
            .build()
        ).toList();
```

### 3. 프론트엔드 타입 정의 추가
```typescript
// types/dog-stats.ts
export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  tags: string;
  type: 'SINGLE';
  sessionId: number;
  sessionDate: string;
  sessionStartTime: string;
  sessionEndTime: string;
  attendanceStatus: 'ATTENDED' | 'ABSENT' | null;  // ⭐ 추가
}
```

---

## 📊 수정 전후 비교

### Before (수정 전)
```json
{
  "trainingApplications": [
    {
      "courseId": 201,
      "courseTitle": "기본 복종 훈련",
      "sessionId": 301,
      "sessionDate": "2024-11-15",
      "sessionStartTime": "10:00:00",
      "sessionEndTime": "11:00:00"
      // ❌ attendanceStatus 없음
    }
  ]
}
```

### After (수정 후)
```json
{
  "trainingApplications": [
    {
      "courseId": 201,
      "courseTitle": "기본 복종 훈련",
      "sessionId": 301,
      "sessionDate": "2024-11-15",
      "sessionStartTime": "10:00:00",
      "sessionEndTime": "11:00:00",
      "attendanceStatus": "ATTENDED"  // ✅ 추가됨
    }
  ]
}
```

---

## 🧪 테스트

### 1. 빌드 확인
```
BUILD SUCCESSFUL in 35s ✅
```

### 2. API 테스트
```bash
curl http://localhost:8080/api/trainer/user/dogs/6 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  | jq '.trainingApplications[0].attendanceStatus'
```

**기대 결과**:
```json
"ATTENDED"  // 또는 "ABSENT" 또는 null
```

### 3. 프론트엔드 확인
```javascript
trainingApplications.map((training) => {
  console.log("단회차 출석 상태:", {
    sessionId: training.sessionId,
    attendanceStatus: training.attendanceStatus,  // ✅ "ATTENDED"
    type: typeof training.attendanceStatus,        // ✅ "string"
  });
});
```

---

## 📝 수정된 파일

1. ✅ `DogStatsResponse.java` - TrainingSessionDto에 attendanceStatus 필드 추가
2. ✅ `TrainerUserService.java` - attendanceStatus 매핑 추가
3. ✅ `FRONTEND_IMPLEMENTATION_GUIDE.md` - TrainingSessionDto 타입 정의 추가

---

## 🎯 영향 범위

### 백엔드
- ✅ 기존 API 호환성 유지 (필드 추가만)
- ✅ 컴파일 에러 없음
- ✅ 빌드 성공

### 프론트엔드
- ✅ 타입 정의 업데이트 필요
- ✅ 기존 코드 수정 불필요 (필드 추가만)
- ✅ 이제 `attendanceStatus` 사용 가능

---

## 🚀 배포

### 백엔드
1. 서버 재시작
```bash
cd C:\mt-server
java -jar build/libs/mt-server-0.0.1-SNAPSHOT.jar
```

### 프론트엔드
1. 타입 정의 업데이트
```typescript
// types/dog-stats.ts에 TrainingSessionDto 추가
```

2. 사용 예시
```typescript
{trainingApplications.map((training) => (
  <div key={training.sessionId}>
    <h3>{training.courseTitle}</h3>
    {/* ✅ 이제 사용 가능! */}
    {training.attendanceStatus === 'ATTENDED' && (
      <span className="text-green-600">✅ 출석</span>
    )}
    {training.attendanceStatus === 'ABSENT' && (
      <span className="text-red-600">❌ 결석</span>
    )}
    {training.attendanceStatus === null && (
      <span className="text-gray-600">📅 예정</span>
    )}
  </div>
))}
```

---

## ✅ 해결 완료

**문제**: 단회차 훈련 출석 상태가 백엔드에서 안 넘어옴  
**원인**: DTO 필드 누락 + Service 매핑 누락  
**해결**: DTO 필드 추가 + Service 매핑 추가  
**상태**: ✅ 완료 (빌드 성공)

---

**수정일**: 2025-12-23  
**영향도**: 낮음 (필드 추가만)  
**호환성**: 기존 API 호환  
**배포**: 서버 재시작 필요

