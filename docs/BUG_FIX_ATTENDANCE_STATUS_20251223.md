# 🚨 긴급 버그 수정: 출석 상태값 불일치

## 📅 발견일: 2025-12-23
## 🔴 심각도: HIGH (출석률 0% 표시)

---

## 🐛 버그 증상

### 실제 로그 데이터
```
multiCourses=[MultiCourseGroupResponse(
  courseId=1,
  totalSessions=3,
  attendedSessions=0,        // ❌ 0으로 표시됨
  attendanceRate=0.0,        // ❌ 0%로 표시됨
  sessions=[
    MultiSessionResponse(sessionId=1, attendanceStatus=ATTENDED),  // ✅ 실제로는 출석함
    MultiSessionResponse(sessionId=2, attendanceStatus=null),
    MultiSessionResponse(sessionId=3, attendanceStatus=null)
  ]
)]
```

**문제**: 
- 실제로 1회차에 출석(`ATTENDED`)했는데
- `attendedSessions=0`, `attendanceRate=0.0`으로 표시됨

---

## 🔍 근본 원인

### SQL 쿼리의 출석 상태값 불일치

**잘못된 쿼리** (`TrainerUserDAO.xml` 200번째 줄):
```sql
COUNT(DISTINCT CASE WHEN ta2.status = 'PRESENT' THEN ts2.session_id END) AS attendedSessions
```

**실제 DB 값**:
```
training_attendance.status = 'ATTENDED'  -- DB에 저장된 값
```

**결과**: 
- SQL은 `'PRESENT'`를 찾지만
- DB에는 `'ATTENDED'`가 저장되어 있어서
- 매칭되는 행이 없으므로 `COUNT = 0`

---

## 🔧 수정 내용

### 1. `findMultiCourseDetail` 쿼리 수정

**Before**:
```sql
COUNT(DISTINCT CASE WHEN ta2.status = 'PRESENT' THEN ts2.session_id END) AS attendedSessions
```

**After**:
```sql
COUNT(DISTINCT CASE WHEN ta2.status = 'ATTENDED' THEN ts2.session_id END) AS attendedSessions
```

### 2. `countAttendedSessions` 쿼리 수정

**Before**:
```sql
WHERE tca.dog_id = #{dogId}
  AND ts.course_id = #{courseId}
  AND ta.status = 'PRESENT'
```

**After**:
```sql
WHERE tca.dog_id = #{dogId}
  AND ts.course_id = #{courseId}
  AND ta.status = 'ATTENDED'
```

### 3. `TrainerUserService.getDogStats()` - 전체 통계 계산 로직 추가 ⭐

**Before**:
```java
// stats는 단회차(SINGLE) 훈련만 집계
return DogStatsResponse.builder()
    .stats(new DogStatsResponse.Stats(timesApplied, attendedCount))  // 단회차만
    .multiCourses(finalGroups)  // 다회차는 별도
    .build();
```

**After**:
```java
// 다회차 통계를 전체 통계에 합산
for (MultiCourseGroupResponse course : multiCourses) {
    timesApplied += (course.getTotalSessions() != null ? course.getTotalSessions() : 0);
    attendedCount += course.getAttendedSessions();
}

log.info("📊 [DogStats] 최종 통계 (단회차+다회차) - timesApplied={}, attendedCount={}", 
        timesApplied, attendedCount);

return DogStatsResponse.builder()
    .stats(new DogStatsResponse.Stats(timesApplied, attendedCount))  // 단회차 + 다회차
    .multiCourses(finalGroups)
    .build();
```

**변경 이유**:
- 기존: `stats`는 단회차만, 다회차는 별도로만 제공
- 문제: 다회차만 신청한 경우 `stats.timesApplied=0, stats.attendedCount=0`
- 해결: **단회차 + 다회차 통계를 합산하여 전체 통계 제공**

---

## 📊 수정 전후 비교

### 시나리오: 다회차 훈련만 신청한 경우 (dogId=6)

**실제 데이터**:
- 다회차 코스 1개, 총 3회차
- 1회차: ATTENDED (출석)
- 2회차: null (예정)
- 3회차: null (예정)

### ❌ 수정 전
```json
{
  "stats": {
    "timesApplied": 0,     // ❌ 다회차가 포함 안 됨
    "attendedCount": 0     // ❌ 다회차가 포함 안 됨
  },
  "multiCourses": [{
    "courseId": 1,
    "totalSessions": 3,
    "attendedSessions": 0,     // ❌ PRESENT를 찾아서 0
    "attendanceRate": 0.0,     // ❌ 0%
    "sessions": [
      { "sessionId": 1, "attendanceStatus": "ATTENDED" },
      { "sessionId": 2, "attendanceStatus": null },
      { "sessionId": 3, "attendanceStatus": null }
    ]
  }]
}
```

**문제점**:
1. `stats`: 단회차만 집계해서 0/0
2. `multiCourses.attendedSessions`: PRESENT 찾아서 0
3. 출석률: 0% (실제로는 33.33%)

### ✅ 수정 후 (기대값)
```json
{
  "stats": {
    "timesApplied": 3,     // ✅ 다회차 포함 (0 + 3)
    "attendedCount": 1     // ✅ 다회차 포함 (0 + 1)
  },
  "multiCourses": [{
    "courseId": 1,
    "totalSessions": 3,
    "attendedSessions": 1,     // ✅ ATTENDED 찾아서 1
    "attendanceRate": 33.33,   // ✅ 33.33% (1/3)
    "sessions": [
      { "sessionId": 1, "attendanceStatus": "ATTENDED" },
      { "sessionId": 2, "attendanceStatus": null },
      { "sessionId": 3, "attendanceStatus": null }
    ]
  }]
}
```

**해결된 문제**:
1. ✅ `stats`: 단회차 + 다회차 합산 (3/1)
2. ✅ `multiCourses.attendedSessions`: ATTENDED로 정확히 계산 (1)
3. ✅ 출석률: 33.33% (정확함)

### 시나리오 2: 단회차 + 다회차 모두 신청한 경우

**데이터**:
- 단회차: 5회 신청, 3회 출석
- 다회차: 3회 신청, 1회 출석

### ❌ 수정 전
```json
{
  "stats": {
    "timesApplied": 5,     // 단회차만
    "attendedCount": 3     // 단회차만
  }
}
```
- 출석률: 60% (5회 중 3회)

### ✅ 수정 후
```json
{
  "stats": {
    "timesApplied": 8,     // 단회차(5) + 다회차(3)
    "attendedCount": 4     // 단회차(3) + 다회차(1)
  }
}
```
- 출석률: 50% (8회 중 4회) ← **더 정확한 전체 출석률**

---

## 🎯 영향 범위

### 영향받는 기능
1. **다회차 훈련 출석률** - 항상 0%로 표시됨
2. **반려견 통계 페이지** - 다회차 훈련 통계 부정확
3. **훈련사 대시보드** - 출석률 차트 부정확

### 영향받지 않는 기능
- **개별 세션 출석 상태** (`attendanceStatus`) - 정상 표시됨
- **단회차 훈련 통계** - 별도 쿼리 사용

---

## 🧪 테스트 방법

### 1. API 호출
```bash
curl -X GET "http://localhost:8080/api/trainer/user/dogs/6" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  | jq '.multiCourses[0]'
```

### 2. 기대 결과
```json
{
  "courseId": 1,
  "totalSessions": 3,
  "attendedSessions": 1,        // ✅ 1이어야 함
  "attendanceRate": 33.33       // ✅ 33.33이어야 함
}
```

### 3. 검증 SQL
```sql
-- 직접 DB에서 확인
SELECT 
    tc.course_id,
    COUNT(DISTINCT ts.session_id) as total_sessions,
    COUNT(DISTINCT CASE WHEN ta.status = 'ATTENDED' THEN ts.session_id END) as attended_sessions
FROM training_course tc
JOIN training_session ts ON tc.course_id = ts.course_id
JOIN training_course_application tca ON ts.session_id = tca.session_id
LEFT JOIN training_attendance ta ON tca.application_id = ta.application_id
WHERE tca.dog_id = 6
  AND tc.type = 'MULTI'
GROUP BY tc.course_id;
```

**기대 결과**:
```
course_id | total_sessions | attended_sessions
----------|----------------|------------------
    1     |       3        |        1
```

---

## 📝 수정된 파일

1. **TrainerUserDAO.xml**
   - Line 200: `'PRESENT'` → `'ATTENDED'`
   - Line 239: `'PRESENT'` → `'ATTENDED'`

2. **TrainerUserService.java** ⭐
   - `getDogStats()` 메서드에 다회차 통계 합산 로직 추가
   - 단회차 + 다회차 통계를 합산하여 전체 통계 제공

---

## ✅ 해결된 이슈

- [x] `attendedSessions`가 항상 0으로 표시되던 문제 해결
- [x] `attendanceRate`가 항상 0.0으로 표시되던 문제 해결
- [x] 출석 상태값 통일 (`ATTENDED` 사용)
- [x] **stats에 다회차 통계도 포함하여 전체 통계 제공** ⭐

---

## 🚀 배포 체크리스트

### 배포 전
- [x] SQL 쿼리 수정
- [x] 빌드 성공 확인
- [ ] 서버 재시작
- [ ] API 테스트
- [ ] 프론트엔드 확인

### 배포 후
- [ ] 기존 데이터 정상 표시 확인
- [ ] 새로운 출석 기록 정상 반영 확인
- [ ] 출석률 계산 정확성 확인

---

## 📌 추가 권장사항

### 1. 출석 상태값 Enum 정의
```java
public enum AttendanceStatus {
    ATTENDED,  // 출석
    ABSENT,    // 결석
    PENDING    // 미정 (예정)
}
```

### 2. DB 제약조건 추가
```sql
ALTER TABLE training_attendance 
ADD CONSTRAINT chk_status 
CHECK (status IN ('ATTENDED', 'ABSENT', 'PENDING'));
```

### 3. 코드 리뷰 시 체크포인트
- [ ] 하드코딩된 상태값 사용 금지
- [ ] Enum 또는 상수 클래스 사용
- [ ] 테스트 케이스에 상태값 검증 포함

---

**작성자**: Backend Team  
**리뷰어**: -  
**승인자**: -  
**배포일**: 2025-12-23 (예정)

---

## 🔗 관련 문서

- [BUG_FIX_DOG_STATS_20250123.md](./BUG_FIX_DOG_STATS_20250123.md) - 이전 버그 수정
- [API_DOG_STATS_DETAIL.md](./API_DOG_STATS_DETAIL.md) - API 상세 문서
- [DOG_STATS_VERIFICATION_GUIDE.md](./DOG_STATS_VERIFICATION_GUIDE.md) - 검증 가이드

