-- ====================================================
-- 반려견 통계 쿼리 성능 최적화 인덱스
-- ====================================================
-- 대상 쿼리: findTrainingApplicationsByDogId
-- 목적: 특정 반려견이 신청한 단회차 훈련 조회 성능 개선
-- ====================================================

-- ====================================================
-- 1. 주요 테이블 분석
-- ====================================================

-- 주요 조인 경로:
-- training_course (c)
--   -> training_session (s)
--   -> training_course_application (a)
--   -> training_attendance (ta)

-- WHERE 조건:
-- - a.dog_id = #{dogId}          ⭐ 가장 중요한 필터
-- - c.is_deleted = 0
-- - a.is_deleted = 0
-- - c.type != 'MULTI'
-- - ta.is_deleted = 0

-- ORDER BY:
-- - c.tags, s.session_date

-- ====================================================
-- 2. 권장 인덱스 (우선순위 순)
-- ====================================================

-- ----------------------------------------------------
-- [최우선] training_course_application 테이블
-- ----------------------------------------------------
-- 이유: WHERE절의 주요 필터링 기준 (dog_id)
-- 효과: 특정 반려견의 신청 내역을 빠르게 조회

-- 복합 인덱스 (권장) ⭐⭐⭐⭐⭐
CREATE INDEX idx_tca_dog_deleted_session
ON training_course_application(dog_id, is_deleted, session_id);

-- 설명:
-- 1. dog_id: 가장 선택도가 높은 컬럼 (특정 반려견)
-- 2. is_deleted: 추가 필터링
-- 3. session_id: 조인 키 포함 (커버링 인덱스)

-- 대안 (더 단순한 버전)
-- CREATE INDEX idx_tca_dog_id ON training_course_application(dog_id);


-- ----------------------------------------------------
-- [우선] training_course 테이블
-- ----------------------------------------------------
-- 이유: type, is_deleted 필터링 및 tags 정렬

-- 복합 인덱스 (권장) ⭐⭐⭐⭐
CREATE INDEX idx_tc_type_deleted_tags
ON training_course(type, is_deleted, tags);

-- 설명:
-- 1. type: WHERE절 필터 (type != 'MULTI')
-- 2. is_deleted: 추가 필터링
-- 3. tags: ORDER BY 정렬 키

-- 또는 course_id를 포함한 버전 (조인 최적화)
CREATE INDEX idx_tc_type_deleted_id
ON training_course(type, is_deleted, course_id);


-- ----------------------------------------------------
-- [중요] training_session 테이블
-- ----------------------------------------------------
-- 이유: 조인 및 정렬 최적화

-- 복합 인덱스 (권장) ⭐⭐⭐
CREATE INDEX idx_ts_course_date
ON training_session(course_id, session_date);

-- 설명:
-- 1. course_id: 조인 키
-- 2. session_date: ORDER BY 정렬 키


-- ----------------------------------------------------
-- [중요] training_attendance 테이블 (서브쿼리용)
-- ----------------------------------------------------
-- 이유: 서브쿼리의 WHERE절 필터링

-- 복합 인덱스 (권장) ⭐⭐⭐⭐
CREATE INDEX idx_ta_app_status_deleted
ON training_attendance(application_id, status, is_deleted);

-- 설명:
-- 1. application_id: 조인 키
-- 2. status: WHERE절 필터 (status = 'ATTENDED')
-- 3. is_deleted: 추가 필터링

-- 서브쿼리 최적화용 (선택사항)
CREATE INDEX idx_ta_status_deleted
ON training_attendance(status, is_deleted)
WHERE status = 'ATTENDED';  -- 부분 인덱스 (PostgreSQL/MySQL 8.0+)


-- ====================================================
-- 3. 기존 인덱스 확인 쿼리
-- ====================================================

-- MySQL 기준
SHOW INDEX FROM training_course_application;
SHOW INDEX FROM training_course;
SHOW INDEX FROM training_session;
SHOW INDEX FROM training_attendance;

-- 또는
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) as COLUMNS,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'your_database_name'  -- 데이터베이스명 변경
  AND TABLE_NAME IN (
    'training_course_application',
    'training_course',
    'training_session',
    'training_attendance'
  )
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE
ORDER BY TABLE_NAME, INDEX_NAME;


-- ====================================================
-- 4. 쿼리 실행 계획 분석
-- ====================================================

-- 인덱스 적용 전 실행 계획
EXPLAIN
SELECT
    c.course_id,
    c.title AS course_title,
    c.tags,
    COUNT(a.application_id) OVER (PARTITION BY c.tags) AS times_applied,
    attended.attended_count,
    ta.`status` AS attendance_status
FROM training_course c
JOIN training_session s ON s.course_id = c.course_id
JOIN training_course_application a ON a.session_id = s.session_id
LEFT JOIN training_attendance ta
    ON ta.application_id = a.application_id
    AND ta.is_deleted = 0
LEFT JOIN (
    SELECT
        c2.tags,
        COUNT(ta2.attendance_id) AS attended_count
    FROM training_course c2
    JOIN training_session s2 ON s2.course_id = c2.course_id
    JOIN training_course_application a2 ON a2.session_id = s2.session_id
    JOIN training_attendance ta2 ON ta2.application_id = a2.application_id
    WHERE ta2.status = 'ATTENDED'
      AND a2.dog_id = 1  -- 테스트용 dogId
      AND a2.is_deleted = 0
      AND c2.is_deleted = 0
    GROUP BY c2.tags
) attended ON attended.tags = c.tags
WHERE a.dog_id = 1  -- 테스트용 dogId
  AND c.is_deleted = 0
  AND a.is_deleted = 0
  AND c.type != 'MULTI'
ORDER BY c.tags, s.session_date;

-- 인덱스 적용 후 다시 확인
-- EXPLAIN 결과에서 다음을 확인:
-- - type: ref, eq_ref (좋음) vs ALL, index (나쁨)
-- - key: 사용된 인덱스 이름
-- - rows: 스캔된 행 수 (적을수록 좋음)
-- - Extra: Using where, Using index (좋음) vs Using filesort, Using temporary (개선 필요)


-- ====================================================
-- 5. 추가 최적화 제안
-- ====================================================

-- ----------------------------------------------------
-- 5-1. FK 인덱스 (아직 없다면 추가)
-- ----------------------------------------------------

-- training_session.course_id (FK)
CREATE INDEX idx_ts_course_id
ON training_session(course_id);

-- training_course_application.session_id (FK)
CREATE INDEX idx_tca_session_id
ON training_course_application(session_id);

-- training_attendance.application_id (FK)
CREATE INDEX idx_ta_application_id
ON training_attendance(application_id);


-- ----------------------------------------------------
-- 5-2. 커버링 인덱스 (고급 최적화)
-- ----------------------------------------------------
-- 쿼리에 필요한 모든 컬럼을 인덱스에 포함하여
-- 테이블 접근 없이 인덱스만으로 쿼리 처리

-- training_course (선택사항)
CREATE INDEX idx_tc_covering
ON training_course(type, is_deleted, course_id, tags, title, description);

-- training_course_application (선택사항)
CREATE INDEX idx_tca_covering
ON training_course_application(dog_id, is_deleted, session_id, application_id);


-- ====================================================
-- 6. 인덱스 적용 순서 (우선순위)
-- ====================================================

-- 1단계: 필수 인덱스 (즉시 적용) ⭐⭐⭐⭐⭐
CREATE INDEX idx_tca_dog_deleted_session
ON training_course_application(dog_id, is_deleted, session_id);

CREATE INDEX idx_tc_type_deleted_tags
ON training_course(type, is_deleted, tags);

-- 2단계: 권장 인덱스 (성능 개선 확인 후 적용) ⭐⭐⭐⭐
CREATE INDEX idx_ts_course_date
ON training_session(course_id, session_date);

CREATE INDEX idx_ta_app_status_deleted
ON training_attendance(application_id, status, is_deleted);

-- 3단계: 선택 인덱스 (필요시 적용) ⭐⭐⭐
-- FK 인덱스들 (이미 있을 수 있음)
-- 커버링 인덱스 (테이블이 매우 큰 경우)


-- ====================================================
-- 7. 성능 테스트
-- ====================================================

-- 인덱스 적용 전 실행 시간 측정
SET profiling = 1;

SELECT ... -- 실제 쿼리

SHOW PROFILES;

-- 인덱스 적용 후 다시 측정하여 비교


-- ====================================================
-- 8. 인덱스 유지보수
-- ====================================================

-- 인덱스 통계 업데이트 (정기적으로 실행)
ANALYZE TABLE training_course_application;
ANALYZE TABLE training_course;
ANALYZE TABLE training_session;
ANALYZE TABLE training_attendance;

-- 인덱스 사용률 확인 (주기적으로 모니터링)
SELECT
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SEQ_IN_INDEX,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'your_database_name'
  AND TABLE_NAME IN (
    'training_course_application',
    'training_course',
    'training_session',
    'training_attendance'
  )
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;


-- ====================================================
-- 9. 주의사항
-- ====================================================

/*
1. 인덱스는 조회 성능을 향상시키지만 INSERT/UPDATE/DELETE 성능은 저하시킵니다.
2. 너무 많은 인덱스는 오히려 역효과를 낼 수 있습니다.
3. 실제 데이터 분포와 쿼리 패턴에 따라 효과가 다를 수 있습니다.
4. 프로덕션 환경에 적용하기 전에 개발/스테이징 환경에서 충분히 테스트하세요.
5. 피크 타임을 피해 인덱스를 생성하세요 (테이블 락 발생 가능).
6. 대용량 테이블의 경우 인덱스 생성에 시간이 오래 걸릴 수 있습니다.
*/

-- MySQL 8.0+ 온라인 DDL (테이블 락 최소화)
-- ALTER TABLE training_course_application
-- ADD INDEX idx_tca_dog_deleted_session (dog_id, is_deleted, session_id),
-- ALGORITHM=INPLACE, LOCK=NONE;


-- ====================================================
-- 10. 모니터링 쿼리
-- ====================================================

-- 인덱스 크기 확인
SELECT
    table_name,
    index_name,
    ROUND(stat_value * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
FROM mysql.innodb_index_stats
WHERE database_name = 'your_database_name'
  AND table_name IN (
    'training_course_application',
    'training_course',
    'training_session',
    'training_attendance'
  )
  AND stat_name = 'size'
ORDER BY size_mb DESC;

-- Slow Query 확인
-- my.cnf 설정:
-- slow_query_log = 1
-- long_query_time = 1
-- slow_query_log_file = /var/log/mysql/slow-query.log

-- 슬로우 쿼리 로그에서 이 쿼리 찾기

