# 📊 반려견 통계 쿼리 인덱스 최적화 가이드

## 🎯 요약

`findTrainingApplicationsByDogId` 쿼리의 성능을 최적화하기 위한 인덱스 제안입니다.

---

## ✅ 필수 인덱스 (즉시 적용 권장)

### 1. training_course_application 테이블 ⭐⭐⭐⭐⭐

```sql
CREATE INDEX idx_tca_dog_deleted_session 
ON training_course_application(dog_id, is_deleted, session_id);
```

**이유**: 
- `WHERE a.dog_id = #{dogId}` - 가장 중요한 필터링 조건
- 특정 반려견의 신청 내역을 빠르게 찾음
- 전체 테이블 스캔 → 인덱스 스캔으로 개선

**효과**: 🚀🚀🚀🚀🚀 (가장 큰 성능 향상)

---

### 2. training_course 테이블 ⭐⭐⭐⭐

```sql
CREATE INDEX idx_tc_type_deleted_tags 
ON training_course(type, is_deleted, tags);
```

**이유**:
- `WHERE c.type != 'MULTI'` - 단회차 훈련 필터링
- `ORDER BY c.tags` - 태그별 정렬
- is_deleted로 논리 삭제 필터링

**효과**: 🚀🚀🚀🚀

---

## 📌 권장 인덱스 (성능 개선)

### 3. training_session 테이블 ⭐⭐⭐

```sql
CREATE INDEX idx_ts_course_date 
ON training_session(course_id, session_date);
```

**이유**:
- 조인 키 (`s.course_id = c.course_id`)
- `ORDER BY s.session_date` - 날짜 정렬

**효과**: 🚀🚀🚀

---

### 4. training_attendance 테이블 ⭐⭐⭐⭐

```sql
CREATE INDEX idx_ta_app_status_deleted 
ON training_attendance(application_id, status, is_deleted);
```

**이유**:
- 서브쿼리에서 `WHERE ta2.status = 'ATTENDED'` 필터링
- `LEFT JOIN` 최적화

**효과**: 🚀🚀🚀🚀

---

## 🚀 즉시 적용 스크립트

```sql
-- 1단계: 필수 인덱스 (성능 향상 큼)
CREATE INDEX idx_tca_dog_deleted_session 
ON training_course_application(dog_id, is_deleted, session_id);

CREATE INDEX idx_tc_type_deleted_tags 
ON training_course(type, is_deleted, tags);

-- 2단계: 권장 인덱스 (추가 성능 개선)
CREATE INDEX idx_ts_course_date 
ON training_session(course_id, session_date);

CREATE INDEX idx_ta_app_status_deleted 
ON training_attendance(application_id, status, is_deleted);

-- 3단계: 통계 업데이트
ANALYZE TABLE training_course_application;
ANALYZE TABLE training_course;
ANALYZE TABLE training_session;
ANALYZE TABLE training_attendance;
```

---

## 📊 성능 비교 (예상)

### Before (인덱스 없음)
- **실행 시간**: 500ms ~ 2000ms
- **스캔 행 수**: 수만 ~ 수십만 행
- **타입**: ALL (풀 테이블 스캔)

### After (인덱스 적용)
- **실행 시간**: 10ms ~ 50ms ⚡
- **스캔 행 수**: 수십 ~ 수백 행
- **타입**: ref, eq_ref (인덱스 사용)

**예상 성능 향상**: 10배 ~ 100배 🚀

---

## 🔍 성능 확인 방법

### 1. 실행 계획 확인

```sql
EXPLAIN 
SELECT ... -- 실제 쿼리
WHERE a.dog_id = 1;
```

**체크 포인트**:
- ✅ `type`: ref, eq_ref (좋음)
- ✅ `key`: idx_tca_dog_deleted_session 사용
- ✅ `rows`: 적은 수 (100 이하가 이상적)
- ❌ `type`: ALL (나쁨 - 인덱스 미사용)

### 2. 실행 시간 측정

```sql
SET profiling = 1;
SELECT ... -- 실제 쿼리
SHOW PROFILES;
```

---

## ⚠️ 주의사항

### 1. 피크 타임 피하기
- 인덱스 생성 시 테이블 락 발생 가능
- 사용자가 적은 시간대에 실행 (새벽 시간 권장)

### 2. 스테이징 환경에서 먼저 테스트
```sql
-- 프로덕션 적용 전 개발/스테이징에서 테스트
EXPLAIN SELECT ...
```

### 3. 디스크 공간 확인
- 인덱스는 추가 디스크 공간 필요
- 테이블 크기의 20~30% 정도 예상

### 4. 온라인 DDL 사용 (MySQL 8.0+)
```sql
ALTER TABLE training_course_application 
ADD INDEX idx_tca_dog_deleted_session (dog_id, is_deleted, session_id),
ALGORITHM=INPLACE, LOCK=NONE;
```

---

## 🎯 우선순위

| 순위 | 테이블 | 인덱스 | 중요도 | 예상 효과 |
|-----|--------|--------|--------|----------|
| 1 | training_course_application | idx_tca_dog_deleted_session | ⭐⭐⭐⭐⭐ | 가장 큼 |
| 2 | training_course | idx_tc_type_deleted_tags | ⭐⭐⭐⭐ | 큼 |
| 3 | training_attendance | idx_ta_app_status_deleted | ⭐⭐⭐⭐ | 큼 |
| 4 | training_session | idx_ts_course_date | ⭐⭐⭐ | 중간 |

---

## 📈 모니터링

### 정기 점검 (월 1회)
```sql
-- 인덱스 사용률 확인
SHOW INDEX FROM training_course_application;

-- 테이블 통계 업데이트
ANALYZE TABLE training_course_application;
```

### Slow Query 로그 확인
```bash
# my.cnf 설정
slow_query_log = 1
long_query_time = 1
```

---

## 📚 상세 문서

- [INDEX_OPTIMIZATION_DOG_STATS.sql](./INDEX_OPTIMIZATION_DOG_STATS.sql) - 전체 인덱스 가이드
- 실행 계획 분석 방법
- 추가 최적화 옵션 (커버링 인덱스 등)

---

## ✅ 체크리스트

### 적용 전
- [ ] 현재 인덱스 확인 (`SHOW INDEX FROM ...`)
- [ ] 현재 쿼리 성능 측정 (실행 시간, EXPLAIN)
- [ ] 디스크 공간 확인
- [ ] 백업 완료

### 적용
- [ ] 스테이징 환경에서 테스트
- [ ] 피크 타임 피해서 적용
- [ ] 필수 인덱스 생성 (1, 2번)
- [ ] 권장 인덱스 생성 (3, 4번)
- [ ] ANALYZE TABLE 실행

### 적용 후
- [ ] EXPLAIN으로 인덱스 사용 확인
- [ ] 실행 시간 재측정
- [ ] 성능 향상 확인 (로그)
- [ ] 모니터링 설정

---

**작성일**: 2025-12-23  
**작성자**: Backend Team  
**관련 쿼리**: `findTrainingApplicationsByDogId`

