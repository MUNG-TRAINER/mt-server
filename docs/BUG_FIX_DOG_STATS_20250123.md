# 🔧 반려견 통계 API 버그 수정 보고서

## 📅 수정일: 2025-01-23

---

## 🚨 발견된 문제

### 1️⃣ **SQL 쿼리 문제 - dogId 조건 누락**

#### 문제점
`TrainerUserDAO.xml`의 `findTrainingApplicationsByDogId` 쿼리에서 `attended_count`를 계산하는 서브쿼리에 `dogId` 조건이 없었습니다.

```sql
-- ❌ 잘못된 쿼리 (수정 전)
LEFT JOIN (
    SELECT
        c2.tags,
        COUNT(ta2.attendance_id) AS attended_count
    FROM training_course c2
    JOIN training_session s2 ON s2.course_id = c2.course_id
    JOIN training_course_application a2 ON a2.session_id = s2.session_id
    JOIN training_attendance ta2 ON ta2.application_id = a2.application_id
    WHERE ta2.status = 'ATTENDED'
    -- ⚠️ dogId 조건 없음! 모든 반려견의 출석 수를 합산
    GROUP BY c2.tags
) attended ON attended.tags = c.tags
```

#### 영향
- **모든 반려견의 출석 수가 합산**되어 잘못된 통계 제공
- 특정 반려견의 실제 출석률을 정확히 계산할 수 없음

#### 수정
```sql
-- ✅ 올바른 쿼리 (수정 후)
LEFT JOIN (
    SELECT
        c2.tags,
        COUNT(ta2.attendance_id) AS attended_count
    FROM training_course c2
    JOIN training_session s2 ON s2.course_id = c2.course_id
    JOIN training_course_application a2 ON a2.session_id = s2.session_id
    JOIN training_attendance ta2 ON ta2.application_id = a2.application_id
    WHERE ta2.status = 'ATTENDED'
      AND a2.dog_id = #{dogId}        -- ⭐ dogId 조건 추가
      AND a2.is_deleted = 0
      AND c2.is_deleted = 0
    GROUP BY c2.tags
) attended ON attended.tags = c.tags
```

---

### 2️⃣ **Service 로직 문제 - 태그별 통계 미합산**

#### 문제점
`TrainerUserService.getDogStats()` 메서드에서 **첫 번째 행의 통계만** 사용했습니다.

```java
// ❌ 잘못된 로직 (수정 전)
Integer timesApplied = singleApps.isEmpty() ? 0 : singleApps.get(0).getTimesApplied();
Integer attendedCount = singleApps.isEmpty() ? 0 : singleApps.get(0).getAttendedCount();
```

#### 영향
- 여러 태그의 훈련을 신청한 경우, **첫 번째 태그의 통계만** 반영
- 예: "기본훈련" 3회 + "행동교정" 2회 → **3회만 표시** (실제로는 5회)

#### 수정
```java
// ✅ 올바른 로직 (수정 후)
int timesApplied = 0;
int attendedCount = 0;

if (!singleApps.isEmpty()) {
    // 태그별로 그룹화하여 중복 제거
    Map<String, TrainingApplicationResponse> tagStats = singleApps.stream()
            .collect(Collectors.toMap(
                    TrainingApplicationResponse::getTags,
                    app -> app,
                    (existing, replacement) -> existing
            ));

    // 모든 태그의 통계 합산
    for (TrainingApplicationResponse app : tagStats.values()) {
        Integer applied = app.getTimesApplied();
        Integer attended = app.getAttendedCount();

        timesApplied += (applied != null ? applied : 0);
        attendedCount += (attended != null ? attended : 0);
    }

    log.info("📊 [DogStats] 전체 통계 - timesApplied={}, attendedCount={}, 태그 수={}", 
            timesApplied, attendedCount, tagStats.size());
}
```

---

### 3️⃣ **로깅 개선**

#### 추가된 로그

**Controller** (`CounselingTrainerController.java`)
```java
log.info("🐕 [API] 반려견 통계 조회 - trainerId={}, dogId={}", trainerId, dogId);
log.info("📊 [Response] stats.timesApplied={}, stats.attendedCount={}", 
        dogStats.getStats().getTimesApplied(), 
        dogStats.getStats().getAttendedCount());
log.info("📋 [Response] trainingApplications.size={}", 
        dogStats.getTrainingApplications() != null ? dogStats.getTrainingApplications().size() : 0);
log.info("📚 [Response] multiCourses.size={}", 
        dogStats.getMultiCourses() != null ? dogStats.getMultiCourses().size() : 0);
```

**Service** (`TrainerUserService.java`)
```java
log.info("🔍 [DogStats] dogId={}, 단회차 신청 건수={}", dogId, singleApps.size());
log.info("📊 [DogStats] 전체 통계 - timesApplied={}, attendedCount={}, 태그 수={}", 
        timesApplied, attendedCount, tagStats.size());
```

---

## 📊 수정 전후 비교

### 시나리오: 반려견이 2개 태그의 훈련을 신청한 경우

**데이터**
- "기본훈련" 태그: 3회 신청, 2회 출석
- "행동교정" 태그: 2회 신청, 1회 출석

#### ❌ 수정 전
```json
{
  "stats": {
    "timesApplied": 3,      // 첫 번째 태그만
    "attendedCount": 100    // 모든 반려견 합산 (잘못된 값!)
  }
}
```

#### ✅ 수정 후
```json
{
  "stats": {
    "timesApplied": 5,      // 3 + 2 = 5 (모든 태그 합산)
    "attendedCount": 3      // 2 + 1 = 3 (해당 반려견만)
  }
}
```

---

## 🔍 테스트 방법

### 1. API 호출
```bash
curl -X GET "http://localhost:8080/api/trainer/user/dogs/{dogId}" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. 로그 확인
```
🐕 [API] 반려견 통계 조회 - trainerId=5, dogId=1
🔍 [DogStats] dogId=1, 단회차 신청 건수=10
📊 [DogStats] 전체 통계 - timesApplied=5, attendedCount=3, 태그 수=2
📊 [Response] stats.timesApplied=5, stats.attendedCount=3
```

### 3. 응답 데이터 검증
- `stats.timesApplied`: 모든 태그의 신청 횟수 합
- `stats.attendedCount`: 해당 반려견의 출석 횟수만
- 출석률: `(3 / 5) * 100 = 60%`

---

## 📝 수정된 파일 목록

1. **TrainerUserDAO.xml**
   - `findTrainingApplicationsByDogId` 쿼리 수정
   - attended_count 서브쿼리에 dogId 조건 추가

2. **TrainerUserService.java**
   - `getDogStats()` 메서드 수정
   - 태그별 통계 합산 로직 추가
   - 디버깅 로그 추가

3. **CounselingTrainerController.java**
   - 응답 데이터 로그 추가
   - `System.out.println` 제거

4. **API_DOG_STATS_DETAIL.md**
   - 통계 계산 방식 설명 추가

---

## ✅ 해결된 이슈

- [x] attended_count에 모든 반려견 데이터가 합산되던 문제 해결
- [x] 첫 번째 태그의 통계만 반영되던 문제 해결
- [x] 태그별 통계를 모두 합산하여 정확한 전체 통계 제공
- [x] null 안전성 확보
- [x] 상세 로깅으로 디버깅 편의성 향상

---

## 🚀 프론트엔드 영향

### 변경 사항 없음
- API 응답 구조는 동일
- TypeScript 인터페이스 변경 없음
- 기존 프론트엔드 코드 그대로 사용 가능

### 개선 사항
- 이제 **정확한 통계 데이터** 제공
- 출석률 계산이 올바르게 작동
- 여러 태그의 훈련을 신청한 경우에도 정확한 전체 통계 표시

---

## 📞 추가 확인 필요 사항

### 다회차 훈련 통계는?
현재 수정은 **단회차 훈련(type != 'MULTI')**에만 적용되었습니다.

만약 **다회차 훈련도 stats에 포함**해야 한다면:
1. `multiCourses`의 출석 정보도 합산 필요
2. Service 로직 추가 수정 필요

### 현재 구현
- `stats`: 단회차 훈련만 (type != 'MULTI')
- `multiCourses`: 다회차 훈련 별도 제공 (attendanceRate 개별 계산)

---

**작성자**: Backend Team  
**날짜**: 2025-01-23  
**버전**: v1.1

