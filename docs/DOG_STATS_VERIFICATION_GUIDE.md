# 🔍 반려견 통계 API 검증 가이드

## 📌 백엔드 수정 사항

### ✅ 수정 완료
1. **SQL 쿼리 수정** (`TrainerUserDAO.xml`)
   - `attended_count` 서브쿼리에 `dogId` 조건 추가
   - 이제 해당 반려견의 출석만 계산

2. **Service 로직 수정** (`TrainerUserService.java`)
   - 태그별 통계를 모두 합산
   - null 안전성 확보
   - 상세 로그 추가

---

## 🧪 테스트 방법

### 1️⃣ 서버 재시작
```bash
cd C:\mt-server
.\gradlew clean build -x test
java -jar build/libs/mt-server-0.0.1-SNAPSHOT.jar
```

### 2️⃣ API 호출
```bash
# 반려견 ID = 1인 경우
curl -X GET "http://localhost:8080/api/trainer/user/dogs/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  | jq '.stats'
```

**기대 응답:**
```json
{
  "timesApplied": 5,
  "attendedCount": 3
}
```

### 3️⃣ 서버 로그 확인

**정상적인 로그:**
```
🐕 [API] 반려견 통계 조회 - trainerId=5, dogId=1
🔍 [DogStats] dogId=1, 단회차 신청 건수=10
📊 [DogStats] 전체 통계 - timesApplied=5, attendedCount=3, 태그 수=2
📊 [Response] stats.timesApplied=5, stats.attendedCount=3
```

**비정상적인 로그 (수정 전):**
```
📊 [Response] stats.timesApplied=3, stats.attendedCount=100
```

---

## 🎨 프론트엔드 검증

### Before (잘못된 데이터)
```typescript
// 문제: attended_count가 비정상적으로 큼
{
  "stats": {
    "timesApplied": 3,      // 첫 번째 태그만
    "attendedCount": 100    // 모든 반려견 합산 ← 틀림!
  }
}
```

### After (올바른 데이터)
```typescript
// 정상: 해당 반려견만, 모든 태그 합산
{
  "stats": {
    "timesApplied": 5,      // 모든 태그 합산 (3 + 2)
    "attendedCount": 3      // 해당 반려견만 (2 + 1)
  }
}
```

### 출석률 계산 확인
```typescript
const attendanceRate = (data.stats.attendedCount / data.stats.timesApplied) * 100;
console.log(`출석률: ${attendanceRate.toFixed(1)}%`);

// 기대값: 60.0% (3 / 5 * 100)
// 비정상: 3333.3% (100 / 3 * 100) ← 수정 전
```

---

## 🚨 체크포인트

### ✅ 확인해야 할 것들

#### 1. 통계 값의 합리성
- [ ] `attendedCount` ≤ `timesApplied` 
  - 출석 수가 신청 수보다 클 수 없음
- [ ] 출석률이 0% ~ 100% 범위 내
  - 100%를 초과하면 비정상

#### 2. 다양한 케이스 테스트

**케이스 1: 단일 태그**
```json
{
  "stats": {
    "timesApplied": 3,
    "attendedCount": 2
  }
}
// 출석률: 66.7%
```

**케이스 2: 여러 태그**
```json
{
  "stats": {
    "timesApplied": 5,   // "기본훈련" 3 + "행동교정" 2
    "attendedCount": 3   // "기본훈련" 2 + "행동교정" 1
  }
}
// 출석률: 60.0%
```

**케이스 3: 신청 없음**
```json
{
  "stats": {
    "timesApplied": 0,
    "attendedCount": 0
  }
}
// 출석률: 0% (나누기 0 방지 처리 필요)
```

#### 3. 프론트엔드 코드 확인

```typescript
// ✅ 올바른 처리
const attendanceRate = data.stats.timesApplied > 0
  ? (data.stats.attendedCount / data.stats.timesApplied) * 100
  : 0;

// ❌ 잘못된 처리 (나누기 0 에러 가능)
const attendanceRate = (data.stats.attendedCount / data.stats.timesApplied) * 100;
```

---

## 🐛 문제 발생 시

### 증상 1: attendedCount가 비정상적으로 큼
**원인**: SQL 쿼리에 `dogId` 조건이 없음  
**확인**: `TrainerUserDAO.xml` 91-106번째 줄 확인  
**해결**: `WHERE` 절에 `AND a2.dog_id = #{dogId}` 추가

### 증상 2: timesApplied가 첫 번째 태그만 반영됨
**원인**: Service에서 첫 번째 값만 사용  
**확인**: `TrainerUserService.java` 100-120번째 줄 확인  
**해결**: 태그별로 그룹화 후 합산

### 증상 3: 로그가 안 나옴
**원인**: `application.yml`의 로그 레벨 설정  
**해결**:
```yaml
logging:
  level:
    com.mungtrainer.mtserver.counseling: INFO
```

---

## 📊 DB 직접 확인 (선택)

### SQL로 검증
```sql
-- test_dog_stats_query.sql 참고
-- dogId=1인 반려견의 실제 통계 확인

-- 태그별 통계
SELECT 
    c.tags,
    COUNT(DISTINCT a.application_id) as times_applied,
    COUNT(CASE WHEN ta.status = 'ATTENDED' THEN 1 END) as attended
FROM training_course c
JOIN training_session s ON s.course_id = c.course_id
JOIN training_course_application a ON a.session_id = s.session_id
LEFT JOIN training_attendance ta ON ta.application_id = a.application_id
WHERE a.dog_id = 1
  AND c.type != 'MULTI'
GROUP BY c.tags;

-- 전체 합산
SELECT 
    COUNT(DISTINCT a.application_id) as total_applied,
    COUNT(CASE WHEN ta.status = 'ATTENDED' THEN 1 END) as total_attended
FROM training_course c
JOIN training_session s ON s.course_id = c.course_id
JOIN training_course_application a ON a.session_id = s.session_id
LEFT JOIN training_attendance ta ON ta.application_id = a.application_id
WHERE a.dog_id = 1
  AND c.type != 'MULTI';
```

---

## 📝 최종 체크리스트

### 백엔드
- [x] SQL 쿼리에 `dogId` 조건 추가
- [x] Service에서 태그별 통계 합산
- [x] 로그 추가
- [ ] 서버 재시작
- [ ] API 테스트

### 프론트엔드
- [ ] API 응답 확인
- [ ] 통계 값 합리성 검증
- [ ] 출석률 계산 정상 확인
- [ ] 나누기 0 에러 방지 확인
- [ ] UI에 정확한 값 표시 확인

---

## 🎯 성공 기준

### ✅ 정상 작동
- `attendedCount` ≤ `timesApplied`
- 출석률이 0% ~ 100% 범위
- 여러 태그의 통계가 모두 합산됨
- 로그에 정확한 값 출력

### ❌ 비정상
- `attendedCount` > `timesApplied`
- 출석률이 100% 초과
- 첫 번째 태그의 값만 반영
- 다른 반려견의 데이터 포함

---

**작성일**: 2025-01-23  
**버전**: v1.1  
**관련 문서**: 
- [BUG_FIX_DOG_STATS_20250123.md](./BUG_FIX_DOG_STATS_20250123.md)
- [API_DOG_STATS_DETAIL.md](./API_DOG_STATS_DETAIL.md)

