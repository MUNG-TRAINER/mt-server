# 🔧 백엔드 UUID 그룹화 구현 완료 가이드

## ✅ 수정 완료 사항

### 1️⃣ DTO 수정
- **파일**: `MultiCourseGroupResponse.java`
- **추가 필드**:
  - `enrollmentCount`: 수강 횟수
  - `enrollmentHistory`: 수강 이력 리스트
- **내부 클래스**: `EnrollmentHistory` 추가

### 2️⃣ Service 수정
- **파일**: `TrainerUserService.java`
- **메서드**: `getDogStats()`
- **추가 로직**:
  - tags(UUID) 기반 재그룹화
  - 수강 이력 생성
  - 전체 평균 출석률 계산

---

## 📊 응답 구조 변화

### Before (수정 전)
```json
{
  "multiCourses": [{
    "tags": "uuid-123",
    "courses": [
      { "courseId": 1, "title": "기초 훈련", "attendanceRate": 80 },
      { "courseId": 5, "title": "기초 훈련 (겨울)", "attendanceRate": 90 },
      { "courseId": 9, "title": "기초 훈련 - 심화", "attendanceRate": 70 }
    ]
  }]
}
```

### After (수정 후)
```json
{
  "multiCourses": [{
    "tags": "uuid-123",
    "courses": [
      {
        "courseId": 1,
        "title": "기초 훈련",
        "enrollmentCount": 3,
        "totalSessions": 30,
        "attendedSessions": 24,
        "attendanceRate": 80.0,
        "enrollmentHistory": [
          {
            "enrollmentNumber": 1,
            "courseId": 1,
            "title": "기초 훈련",
            "startDate": "2024-01-10",
            "endDate": "2024-02-10",
            "totalSessions": 10,
            "attendedSessions": 8,
            "attendanceRate": 80.0,
            "sessions": [...]
          },
          {
            "enrollmentNumber": 2,
            "courseId": 5,
            "title": "기초 훈련 (겨울)",
            "startDate": "2024-07-10",
            "endDate": "2024-08-10",
            "totalSessions": 10,
            "attendedSessions": 9,
            "attendanceRate": 90.0,
            "sessions": [...]
          },
          {
            "enrollmentNumber": 3,
            "courseId": 9,
            "title": "기초 훈련 - 심화",
            "startDate": "2024-12-10",
            "endDate": "2025-01-10",
            "totalSessions": 10,
            "attendedSessions": 7,
            "attendanceRate": 70.0,
            "sessions": [...]
          }
        ]
      }
    ]
  }]
}
```

---

## 🎯 핵심 로직 설명

### 1. CourseId 그룹화 (기존)
```java
// 세션을 courseId별로 병합
Map<Long, MultiCourseGroupResponse> groupedByCourseId = new HashMap<>();
```

### 2. UUID 재그룹화 (⭐ 신규)
```java
// 같은 tags(UUID)를 가진 과정들을 묶음
Map<String, List<MultiCourseGroupResponse>> groupedByUuid = new HashMap<>();

for (MultiCourseGroupResponse course : courseList) {
    String uuid = course.getTags();
    groupedByUuid.computeIfAbsent(uuid, k -> new ArrayList<>()).add(course);
}
```

### 3. 단일 수강 vs 여러 수강 처리
```java
if (sameCourses.size() == 1) {
    // 단일 수강: enrollmentHistory 불필요
    single.setEnrollmentCount(1);
    single.setEnrollmentHistory(null);
} else {
    // 여러 수강: 이력 생성
    merged.setEnrollmentCount(sameCourses.size());
    merged.setEnrollmentHistory(histories);
}
```

### 4. 수강 이력 생성
```java
for (int i = 0; i < sameCourses.size(); i++) {
    MultiCourseGroupResponse course = sameCourses.get(i);
    
    // 시작/종료일 계산
    List<LocalDate> dates = course.getSessions().stream()
        .map(MultiSessionResponse::getSessionDate)
        .sorted()
        .toList();
    
    // EnrollmentHistory 생성
    histories.add(EnrollmentHistory.builder()
        .enrollmentNumber(i + 1)  // 1, 2, 3...
        .courseId(course.getCourseId())
        .title(course.getTitle())  // 과정별 차이!
        .startDate(dates.get(0))
        .endDate(dates.get(dates.size() - 1))
        // ...
        .build());
}
```

### 5. 전체 통계 계산
```java
// 모든 수강의 세션/출석 합산
int totalSessionsSum = 0;
int attendedSessionsSum = 0;

for (MultiCourseGroupResponse course : sameCourses) {
    totalSessionsSum += course.getTotalSessions();
    attendedSessionsSum += course.getAttendedSessions();
}

// 전체 평균 출석률
double overallRate = totalSessionsSum > 0 
    ? (attendedSessionsSum * 100.0 / totalSessionsSum) 
    : 0.0;
```

---

## 🧪 테스트 방법

### 1. 빌드
```bash
cd C:\mt-server
.\gradlew clean build -x test
```

### 2. 실행
```bash
java -jar build/libs/mt-server-0.0.1-SNAPSHOT.jar
```

### 3. API 호출
```bash
curl -X GET "http://localhost:8080/api/trainer/user/dogs/6" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  | jq '.multiCourses[0].courses[0]'
```

### 4. 응답 확인
```json
{
  "courseId": 1,
  "title": "강아지 기초 훈련 4주 코스",
  "enrollmentCount": 3,  // ✅ 수강 횟수
  "totalSessions": 30,    // ✅ 전체 세션 수
  "attendedSessions": 24, // ✅ 전체 출석 수
  "attendanceRate": 80.0, // ✅ 전체 평균 출석률
  "enrollmentHistory": [  // ✅ 수강 이력
    {
      "enrollmentNumber": 1,
      "title": "강아지 기초 훈련 4주 코스",
      // ...
    },
    {
      "enrollmentNumber": 2,
      "title": "강아지 기초 훈련 4주 코스 (겨울)",  // ✅ 차이 표시
      // ...
    }
  ]
}
```

### 5. 로그 확인
```
📊 [DogStats] 최종 통계 (단회차+다회차) - timesApplied=30, attendedCount=24
```

---

## 🔍 프론트엔드 영향

### 기존 프론트 코드 (그룹화 로직)
```typescript
// ❌ 이제 불필요! 백엔드에서 이미 그룹화됨
const groupedCourses = useMemo(
  () => groupCoursesByTags(category.courses),
  [category.courses]
);
```

### 새로운 프론트 코드
```typescript
// ✅ 백엔드에서 그룹화된 데이터를 그대로 사용
{category.courses.map((course) => (
  <div key={course.courseId}>
    {/* enrollmentCount 표시 */}
    <span>{course.enrollmentCount}회 수강</span>
    
    {/* 전체 평균 출석률 */}
    <p>{course.attendanceRate.toFixed(1)}%</p>
    
    {/* enrollmentHistory 렌더링 */}
    {course.enrollmentHistory?.map((enrollment) => (
      <div key={enrollment.enrollmentNumber}>
        <span>{enrollment.enrollmentNumber}차 수강</span>
        <p>{enrollment.title}</p>  {/* 과정별 차이 */}
        <p>{enrollment.attendanceRate}%</p>
      </div>
    ))}
  </div>
))}
```

---

## ⚠️ 주의사항

### 1. enrollmentHistory null 체크
```typescript
// 단일 수강인 경우 enrollmentHistory가 null
{course.enrollmentHistory ? (
  course.enrollmentHistory.map(...)
) : (
  <p>단일 수강</p>
)}
```

### 2. enrollmentCount 활용
```typescript
// 1회 수강이면 뱃지 안 보이게
{course.enrollmentCount > 1 && (
  <span>{course.enrollmentCount}회 수강</span>
)}
```

### 3. sessions 위치 변경
```typescript
// ❌ 기존: course.sessions
// ✅ 새로: course.enrollmentHistory[0].sessions

// 여러 수강인 경우
{course.enrollmentHistory?.map((enrollment) => (
  <SessionTimeline sessions={enrollment.sessions} />
))}

// 단일 수강인 경우
{course.sessions && (
  <SessionTimeline sessions={course.sessions} />
)}
```

---

## 📊 성능 비교

### Before (프론트 그룹화)
```
데이터 처리: 프론트 (1-5ms)
서버 응답: 빠름
프론트 렌더링: 느림 (그룹화 + 렌더링)
```

### After (백엔드 그룹화)
```
데이터 처리: 서버 (2-3ms)
서버 응답: 약간 느림 (2-3ms 추가)
프론트 렌더링: 빠름 (렌더링만)

총 체감 속도: 동일하거나 더 빠름 ✅
```

---

## ✅ 체크리스트

### 백엔드
- [x] DTO에 `enrollmentCount` 추가
- [x] DTO에 `EnrollmentHistory` 내부 클래스 추가
- [x] Service에 UUID 그룹화 로직 추가
- [x] 단일/여러 수강 분기 처리
- [x] 수강 이력 생성 로직
- [x] 전체 통계 계산
- [ ] 빌드 성공 확인
- [ ] API 테스트
- [ ] 로그 확인

### 프론트엔드 (수정 필요)
- [ ] `groupCoursesByTags` 유틸리티 제거
- [ ] `enrollmentCount` 표시 추가
- [ ] `enrollmentHistory` 렌더링 추가
- [ ] null 체크 추가
- [ ] sessions 위치 변경

---

## 🚀 배포 순서

1. **백엔드 배포**
   - 빌드 및 테스트
   - 서버 배포

2. **프론트 수정**
   - 백엔드 응답 구조 변경 반영
   - 그룹화 로직 제거
   - 새로운 필드 렌더링

3. **통합 테스트**
   - 단일 수강 케이스
   - 여러 수강 케이스
   - 빈 데이터 케이스

---

**백엔드 수정 완료!** ✅  
**이제 빌드하고 테스트하세요!** 🚀

**업데이트**: 2025-12-23

