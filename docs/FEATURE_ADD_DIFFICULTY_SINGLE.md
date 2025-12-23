# ✅ 단회차 훈련 난이도 필드 추가 완료

## 📅 수정일: 2025-12-23

---

## 🎯 요구사항

> **"난이도도 추가해줘"**

단회차 훈련(`trainingApplications`)에 난이도(`difficulty`) 필드를 추가했습니다.

---

## ✅ 수정 내용

### 1. TrainingApplicationResponse.java (MyBatis DTO)
```java
@Getter
@Setter
@Builder
public class TrainingApplicationResponse {
    // 과정 정보
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String tags;
    private String type;
    private String difficulty;  // ⭐ 추가 (BEGINNER, INTERMEDIATE, ADVANCED)
    
    // ... 나머지 필드
}
```

### 2. TrainerUserDAO.xml (SQL 쿼리)
```xml
<select id="findTrainingApplicationsByDogId">
    SELECT
        c.course_id,
        c.title AS course_title,
        c.description AS course_description,
        c.tags,
        c.type,
        c.difficulty,  -- ⭐ 추가
        
        s.session_id,
        s.session_date,
        -- ...
    FROM training_course c
    -- ...
</select>
```

### 3. DogStatsResponse.TrainingSessionDto (응답 DTO)
```java
@Getter @Setter @ToString @Builder
public static class TrainingSessionDto {
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String tags;
    private String type;
    private String difficulty;  // ⭐ 추가
    private Long sessionId;
    private LocalDate sessionDate;
    private LocalTime sessionStartTime;
    private LocalTime sessionEndTime;
    private String attendanceStatus;
}
```

### 4. TrainerUserService.java (매핑)
```java
List<DogStatsResponse.TrainingSessionDto> simplified =
    singleApps.stream()
        .map(item -> DogStatsResponse.TrainingSessionDto.builder()
            .courseId(item.getCourseId())
            .courseTitle(item.getCourseTitle())
            .courseDescription(item.getCourseDescription())
            .tags(item.getTags())
            .type(item.getType())
            .difficulty(item.getDifficulty())  // ⭐ 매핑 추가
            .sessionId(item.getSessionId())
            .sessionDate(item.getSessionDate())
            .sessionStartTime(item.getSessionStartTime())
            .sessionEndTime(item.getSessionEndTime())
            .attendanceStatus(item.getAttendanceStatus())
            .build()
        ).toList();
```

---

## 📊 응답 예시

### Before (수정 전)
```json
{
  "trainingApplications": [{
    "courseId": 201,
    "courseTitle": "기본 복종 훈련",
    "tags": "기본훈련",
    "type": "SINGLE",
    "sessionId": 301,
    "sessionDate": "2024-11-15",
    "attendanceStatus": "ATTENDED"
  }]
}
```

### After (수정 후)
```json
{
  "trainingApplications": [{
    "courseId": 201,
    "courseTitle": "기본 복종 훈련",
    "tags": "기본훈련",
    "type": "SINGLE",
    "difficulty": "BEGINNER",  // ⭐ 추가
    "sessionId": 301,
    "sessionDate": "2024-11-15",
    "attendanceStatus": "ATTENDED"
  }]
}
```

---

## 💻 프론트엔드 타입 정의

```typescript
// types/dog-stats.ts

export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  tags: string;
  type: 'SINGLE';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';  // ⭐ 추가
  sessionId: number;
  sessionDate: string;
  sessionStartTime: string;
  sessionEndTime: string;
  attendanceStatus: 'ATTENDED' | 'ABSENT' | null;
}
```

---

## 🎨 프론트엔드 사용 예시

```typescript
// 난이도 뱃지 설정
const difficultyConfig = {
  BEGINNER: { label: '초급', color: 'bg-green-100 text-green-800' },
  INTERMEDIATE: { label: '중급', color: 'bg-yellow-100 text-yellow-800' },
  ADVANCED: { label: '고급', color: 'bg-red-100 text-red-800' },
};

// 렌더링
{trainingApplications.map((training) => (
  <div key={training.sessionId}>
    <h3>{training.courseTitle}</h3>
    
    {/* 난이도 뱃지 */}
    <span className={`px-2 py-1 rounded text-xs ${difficultyConfig[training.difficulty].color}`}>
      {difficultyConfig[training.difficulty].label}
    </span>
    
    {/* 출석 상태 */}
    {training.attendanceStatus === 'ATTENDED' && (
      <span className="text-green-600">✅ 출석</span>
    )}
  </div>
))}
```

---

## 🧪 빌드 결과

```
BUILD SUCCESSFUL in 18s ✅
6 actionable tasks: 6 executed
```

---

## 📝 수정된 파일

1. ✅ `TrainingApplicationResponse.java` - difficulty 필드 추가
2. ✅ `TrainerUserDAO.xml` - SQL SELECT에 difficulty 추가
3. ✅ `DogStatsResponse.java` - TrainingSessionDto에 difficulty 추가
4. ✅ `TrainerUserService.java` - difficulty 매핑 추가
5. ✅ `FRONTEND_IMPLEMENTATION_GUIDE.md` - 타입 정의 업데이트

---

## 🚀 배포

### 백엔드
```bash
# 서버 재시작
cd C:\mt-server
java -jar build/libs/mt-server-0.0.1-SNAPSHOT.jar
```

### 프론트엔드
```typescript
// types/dog-stats.ts 업데이트
export interface TrainingSessionDto {
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';  // 추가
}
```

---

## ✅ 완료

- ✅ 백엔드: difficulty 필드 추가
- ✅ SQL 쿼리: difficulty 조회
- ✅ Service: difficulty 매핑
- ✅ 빌드: 성공
- ✅ 문서: 프론트 가이드 업데이트

**이제 단회차 훈련에서도 난이도를 확인할 수 있습니다!** 🎉

---

**수정일**: 2025-12-23  
**빌드**: ✅ SUCCESS  
**영향도**: 낮음 (필드 추가만)

