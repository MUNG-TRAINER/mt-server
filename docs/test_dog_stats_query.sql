-- 반려견 통계 테스트 쿼리
-- dogId = 1 인 경우를 가정

-- ========================================
-- 1. 단회차 훈련 통계
-- ========================================

-- 1-1. 단회차 신청 현황 (수정된 쿼리)
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
      AND a2.dog_id = 1  -- 특정 반려견만!
      AND a2.is_deleted = 0
      AND c2.is_deleted = 0
    GROUP BY c2.tags
) attended ON attended.tags = c.tags
WHERE a.dog_id = 1
  AND c.is_deleted = 0
  AND a.is_deleted = 0
  AND c.type != 'MULTI'
ORDER BY c.tags, s.session_date;

-- 1-2. 태그별 통계 확인
SELECT
    c.tags,
    COUNT(DISTINCT a.application_id) as times_applied,
    COUNT(CASE WHEN ta.status = 'ATTENDED' THEN 1 END) as attended_count
FROM training_course c
JOIN training_session s ON s.course_id = c.course_id
JOIN training_course_application a ON a.session_id = s.session_id
LEFT JOIN training_attendance ta ON ta.application_id = a.application_id
WHERE a.dog_id = 1
  AND c.is_deleted = 0
  AND a.is_deleted = 0
  AND c.type != 'MULTI'
GROUP BY c.tags;

-- 1-3. 전체 통계 (기대값)
SELECT
    COUNT(DISTINCT a.application_id) as total_times_applied,
    COUNT(CASE WHEN ta.status = 'ATTENDED' THEN 1 END) as total_attended_count
FROM training_course c
JOIN training_session s ON s.course_id = c.course_id
JOIN training_course_application a ON a.session_id = s.session_id
LEFT JOIN training_attendance ta ON ta.application_id = a.application_id
WHERE a.dog_id = 1
  AND c.is_deleted = 0
  AND a.is_deleted = 0
  AND c.type != 'MULTI';

-- ========================================
-- 2. 다회차 훈련 통계 (⭐ 출석 상태값 버그 수정)
-- ========================================

-- 2-1. 다회차 출석률 확인 (수정된 쿼리)
SELECT
    tc.course_id,
    tc.title,
    COUNT(DISTINCT ts.session_id) as total_sessions,
    COUNT(DISTINCT CASE WHEN ta.status = 'ATTENDED' THEN ts.session_id END) as attended_sessions,
    CASE
        WHEN COUNT(DISTINCT ts.session_id) = 0 THEN 0
        ELSE COUNT(DISTINCT CASE WHEN ta.status = 'ATTENDED' THEN ts.session_id END) * 100.0 / COUNT(DISTINCT ts.session_id)
    END as attendance_rate
FROM training_course tc
JOIN training_session ts ON tc.course_id = ts.course_id
JOIN training_course_application tca ON ts.session_id = tca.session_id
LEFT JOIN training_attendance ta ON tca.application_id = ta.application_id
WHERE tca.dog_id = 1
  AND tc.type = 'MULTI'
  AND tc.is_deleted = 0
GROUP BY tc.course_id, tc.title;

-- 2-2. 세션별 출석 상태 확인
SELECT
    tc.course_id,
    tc.title,
    ts.session_no,
    ts.session_date,
    ta.status as attendance_status
FROM training_course tc
JOIN training_session ts ON tc.course_id = ts.course_id
JOIN training_course_application tca ON ts.session_id = tca.session_id
LEFT JOIN training_attendance ta ON tca.application_id = ta.application_id
WHERE tca.dog_id = 1
  AND tc.type = 'MULTI'
ORDER BY tc.course_id, ts.session_no;

-- ========================================
-- 3. 출석 상태값 검증
-- ========================================

-- 3-1. 사용 중인 출석 상태값 확인
SELECT DISTINCT status
FROM training_attendance
WHERE is_deleted = 0;

-- 예상 결과: 'ATTENDED', 'ABSENT', NULL 등

-- 3-2. dogId=6인 반려견의 출석 상태
SELECT
    tca.application_id,
    ta.status,
    ts.session_no,
    tc.title
FROM training_attendance ta
JOIN training_course_application tca ON ta.application_id = tca.application_id
JOIN training_session ts ON tca.session_id = ts.session_id
JOIN training_course tc ON ts.course_id = tc.course_id
WHERE tca.dog_id = 6
ORDER BY tc.course_id, ts.session_no;

