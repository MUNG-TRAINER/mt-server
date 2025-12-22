# 달력 기반 훈련 과정 조회 API 구현 가이드

## 📋 개요

**CourseSearchResponse를 그대로 사용**하면서 달력 기능을 추가했습니다.
- 달력에는 **회차(세션) 기반**으로 날짜를 표시합니다
- 달력에서 날짜를 클릭하면 **해당 날짜의 코스 리스트**를 `CourseSearchResponse` 형식으로 조회합니다
- 역할별로 자동 필터링됩니다 (USER: 소속 훈련사, TRAINER: 본인 등록 과정)

## 🎯 구현된 API

### 1️⃣ 달력용 API - 세션 날짜 조회
**엔드포인트**: `GET /api/course/calendar`

특정 기간(예: 한 달)의 세션이 있는 날짜 목록을 조회합니다.

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| startDate | LocalDate | ✅ | 시작 날짜 (yyyy-MM-dd) |
| endDate | LocalDate | ✅ | 종료 날짜 (yyyy-MM-dd) |
| keyword | String | ❌ | 검색 키워드 (제목, 설명, 태그) |
| lessonForm | String | ❌ | 수업 형태 (WALK, GROUP, PRIVATE) |

#### 응답 예시
```json
{
  "sessionDates": [
    {
      "sessionDate": "2024-01-15",
      "sessionCount": 3
    },
    {
      "sessionDate": "2024-01-20",
      "sessionCount": 2
    }
  ],
  "totalDates": 2
}
```

#### 사용 예시
```bash
# 2024년 1월 전체 조회
GET /api/course/calendar?startDate=2024-01-01&endDate=2024-01-31

# 키워드 필터링
GET /api/course/calendar?startDate=2024-01-01&endDate=2024-01-31&keyword=기초

# 수업 형태 필터링
GET /api/course/calendar?startDate=2024-01-01&endDate=2024-01-31&lessonForm=WALK
```

---

### 2️⃣ 특정 날짜의 코스 리스트 API
**엔드포인트**: `GET /api/course/calendar/courses`

선택한 날짜에 진행되는 모든 코스를 **CourseSearchResponse 형식**으로 조회합니다.

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| date | LocalDate | ✅ | 조회할 날짜 (yyyy-MM-dd) |
| keyword | String | ❌ | 검색 키워드 (제목, 설명, 태그) |
| lessonForm | String | ❌ | 수업 형태 (WALK, GROUP, PRIVATE) |

#### 응답 예시 (CourseSearchResponse 형식)
```json
{
  "courses": [
    {
      "courseId": 123,
      "trainerId": 789,
      "trainerName": "김훈련",
      "title": "강아지 기초 훈련",
      "description": "산책 예절 및 기본 명령어 훈련",
      "tags": "산책,기초,예절",
      "mainImage": "https://presigned-url...",
      "type": "MULTI",
      "lessonForm": "WALK",
      "status": "SCHEDULED",
      "difficulty": "BEGINNER",
      "isFree": false,
      "location": "서울시 강남구",
      "schedule": "매주 월,수,금",
      "dogSize": "SMALL",
      "session": {
        "sessionId": 456,
        "startTime": "2024-01-15T10:00:00",
        "endTime": "2024-01-15T11:00:00",
        "locationDetail": "강남역 3번 출구",
        "maxStudents": 5,
        "price": 50000
      }
    }
  ],
  "hasMore": false,
  "lastCourseId": null,
  "size": 1
}
```

#### 사용 예시
```bash
# 특정 날짜의 모든 코스 조회
GET /api/course/calendar/courses?date=2024-01-15

# 키워드 필터링
GET /api/course/calendar/courses?date=2024-01-15&keyword=기초

# 수업 형태 필터링
GET /api/course/calendar/courses?date=2024-01-15&lessonForm=WALK
```

---

## 🏗️ 구현 구조

### 생성/수정된 파일 목록

#### DTO (2개)
✅ `CalendarSessionDateDto.java` - 달력 날짜 정보  
✅ `CalendarResponse.java` - 달력 조회 응답

#### Controller (수정)
✅ `TrainingCourseController.java`
- `GET /api/course/calendar` - 달력 조회
- `GET /api/course/calendar/courses` - 날짜별 코스 조회

#### Service (수정)
✅ `TrainingCourseService.java`
- `getCalendarByPeriod()` - 달력 조회
- `getCoursesByDate()` - 특정 날짜 코스 조회 (CourseSearchResponse 반환)

#### DAO (수정)
✅ `TrainingSessionDAO.java` - 세션 날짜 조회 메서드 추가
✅ `TrainingCourseDao.java` - 날짜별 코스 조회 메서드 추가

#### Mapper XML (수정)
✅ `TrainingSessionMapper.xml` - 세션 날짜 목록 쿼리
✅ `TrainingCourseMapper.xml` - 날짜별 코스 조회 쿼리

---

## 🔐 권한 관리

### 역할별 필터링
- **USER (일반 사용자)**: 자신이 속한 훈련사의 과정만 조회
- **TRAINER (훈련사)**: 자신이 등록한 과정만 조회

---

## 🗄️ 데이터베이스 쿼리

### 1. 세션 날짜 목록 조회 쿼리

```sql
SELECT
    ts.session_date AS sessionDate,
    COUNT(DISTINCT ts.session_id) AS sessionCount
FROM training_session ts
INNER JOIN training_course tc ON ts.course_id = tc.course_id
    AND tc.is_deleted = 0
WHERE ts.is_deleted = 0
    AND ts.session_date BETWEEN #{startDate} AND #{endDate}
    -- 역할별 필터링
    AND tc.trainer_id = #{trainerId}  (조건부)
    -- 키워드 검색
    AND (tc.title LIKE CONCAT('%', #{keyword}, '%') OR ...) (조건부)
    -- 수업 형태 필터링
    AND tc.lesson_form = #{lessonForm}  (조건부)
GROUP BY ts.session_date
ORDER BY ts.session_date ASC
```

### 2. 특정 날짜의 코스 목록 조회 쿼리

```sql
SELECT
    tc.course_id,
    tc.trainer_id,
    u.name AS trainer_name,
    tc.title,
    tc.description,
    -- ... 기타 코스 정보
    ts.session_id,
    ts.start_time,
    ts.end_time,
    ts.location_detail,
    ts.max_students,
    ts.price
FROM training_course tc
INNER JOIN user u ON tc.trainer_id = u.user_id AND u.is_deleted = 0
INNER JOIN training_session ts ON tc.course_id = ts.course_id
    AND ts.is_deleted = 0
    AND ts.session_date = #{date}
WHERE tc.is_deleted = 0
    -- 역할별 필터링 및 검색 조건
ORDER BY ts.start_time ASC, tc.title ASC
```

---

## 📊 사용 시나리오

### 프론트엔드 연동 예시

#### 1단계: 달력 렌더링
```javascript
// 2024년 1월 달력 데이터 가져오기
const response = await fetch('/api/course/calendar?startDate=2024-01-01&endDate=2024-01-31');
const data = await response.json();

// 달력에 표시
data.sessionDates.forEach(item => {
  // item.sessionDate 날짜에 점 또는 배지 표시
  // item.sessionCount로 세션 개수 표시 가능
});
```

#### 2단계: 날짜 클릭 시 코스 목록 조회
```javascript
// 사용자가 1월 15일 클릭
const selectedDate = '2024-01-15';
const response = await fetch(`/api/course/calendar/courses?date=${selectedDate}`);
const data = await response.json();  // CourseSearchResponse 형식

// 기존 검색 결과와 동일한 형식으로 렌더링
data.courses.forEach(course => {
  console.log(course.title, course.session.startTime, course.session.price);
});
```

---

## ⚠️ 주요 특징

### 1. CourseSearchResponse 재사용
- 기존 무한 스크롤 검색과 **동일한 응답 형식** 사용
- 프론트엔드에서 **같은 컴포넌트로 렌더링** 가능
- `hasMore`는 항상 `false` (날짜별 조회는 페이지네이션 없음)

### 2. 날짜 형식
- 모든 날짜는 `yyyy-MM-dd` 형식 사용 (ISO 8601)
- 예: `2024-01-15`

### 3. 에러 코드
- `INVALID_DATE_RANGE` (400): 시작 날짜가 종료 날짜보다 뒤인 경우
- `INVALID_LESSON_FORM` (400): 유효하지 않은 수업 형태

### 4. S3 Presigned URL
- `mainImage`는 자동으로 Presigned URL로 변환됩니다

---

## 🧪 테스트 가이드

### API 테스트 (Postman/cURL)

```bash
# 1. 달력 조회
curl -X GET "http://localhost:8080/api/course/calendar?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer {access_token}"

# 2. 특정 날짜 코스 조회
curl -X GET "http://localhost:8080/api/course/calendar/courses?date=2024-01-15" \
  -H "Authorization: Bearer {access_token}"

# 3. 필터링 적용
curl -X GET "http://localhost:8080/api/course/calendar/courses?date=2024-01-15&keyword=기초&lessonForm=WALK" \
  -H "Authorization: Bearer {access_token}"
```

---

## ✅ 체크리스트

구현 완료 항목:
- [x] DTO 클래스 생성 (2개)
- [x] TrainingCourseController에 엔드포인트 추가 (2개)
- [x] TrainingCourseService에 메서드 추가 (2개)
- [x] DAO 메서드 추가 (2개)
- [x] Mapper XML 쿼리 추가 (2개)
- [x] 에러 코드 추가
- [x] 역할별 필터링 구현
- [x] S3 Presigned URL 처리
- [x] CourseSearchResponse 형식 재사용
- [x] 빌드 성공 확인

---

## 📌 API 엔드포인트 요약

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/course/search` | 코스 검색 (무한 스크롤) |
| GET | `/api/course/{courseId}` | 코스 상세 조회 |
| GET | `/api/course/calendar` | **달력 조회 (세션 날짜 목록)** |
| GET | `/api/course/calendar/courses` | **특정 날짜의 코스 목록** |

---

**구현 완료일**: 2024-12-22  
**작성자**: GitHub Copilot

