# 🗓️ 달력 기반 훈련 과정 조회 API 명세서

> **프론트엔드 개발자를 위한 API 가이드**  
> 검색 페이지에서 달력 뷰를 구현할 때 사용하는 API입니다.

---

## 📌 개요

달력 기반 검색은 **2단계**로 동작합니다:

1. **1단계**: 달력에 세션이 있는 날짜 표시 (점 또는 배지)
2. **2단계**: 사용자가 날짜를 클릭하면 해당 날짜의 코스 목록 표시

### 주요 특징
- ✅ 기존 `CourseSearchResponse`와 **동일한 형식** 사용
- ✅ 기존 코스 리스트 컴포넌트 **재사용 가능**
- ✅ 역할별 자동 필터링 (USER/TRAINER)
- ✅ 키워드 및 수업 형태 필터링 지원

---

## 🎯 API 1: 달력 조회 (세션 날짜 목록)

### 기본 정보

```
GET /api/course/calendar
```

**용도**: 특정 기간(예: 한 달)의 세션이 있는 날짜 목록을 조회합니다.  
**사용 시점**: 달력을 렌더링할 때 사용

### 요청

#### Headers
```http
Authorization: Bearer {access_token}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `startDate` | string | ✅ | 시작 날짜 (yyyy-MM-dd) | `2024-01-01` |
| `endDate` | string | ✅ | 종료 날짜 (yyyy-MM-dd) | `2024-01-31` |
| `keyword` | string | ❌ | 검색 키워드 | `기초 훈련` |
| `lessonForm` | string | ❌ | 수업 형태<br/>`WALK`, `GROUP`, `PRIVATE` | `WALK` |

### 응답

#### 성공 응답 (200 OK)

```json
{
  "sessionDates": [
    {
      "sessionDate": "2024-01-05",
      "sessionCount": 2
    },
    {
      "sessionDate": "2024-01-12",
      "sessionCount": 1
    },
    {
      "sessionDate": "2024-01-15",
      "sessionCount": 3
    },
    {
      "sessionDate": "2024-01-20",
      "sessionCount": 1
    }
  ],
  "totalDates": 4
}
```

#### 응답 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `sessionDates` | array | 세션이 있는 날짜 목록 |
| `sessionDates[].sessionDate` | string | 세션 날짜 (yyyy-MM-dd) |
| `sessionDates[].sessionCount` | number | 해당 날짜의 세션 개수 |
| `totalDates` | number | 세션이 있는 총 날짜 수 |

#### 에러 응답

**400 Bad Request** - 날짜 범위 오류
```json
{
  "status": 400,
  "message": "시작 날짜는 종료 날짜보다 이전이어야 합니다."
}
```

**400 Bad Request** - 잘못된 수업 형태
```json
{
  "status": 400,
  "message": "훈련 형태가 유효하지 않습니다. 허용된 값: WALK, GROUP, PRIVATE"
}
```

### 사용 예시

#### JavaScript (Fetch API)

```javascript
// 2024년 1월 달력 데이터 가져오기
async function loadCalendar() {
  const params = new URLSearchParams({
    startDate: '2024-01-01',
    endDate: '2024-01-31'
  });

  const response = await fetch(`/api/course/calendar?${params}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });

  const data = await response.json();
  
  // 달력에 표시
  data.sessionDates.forEach(({ sessionDate, sessionCount }) => {
    // 해당 날짜에 점 또는 배지 표시
    markCalendarDate(sessionDate, sessionCount);
  });
}
```

#### JavaScript (키워드 필터링)

```javascript
// "산책" 키워드로 필터링
async function loadCalendarWithFilter() {
  const params = new URLSearchParams({
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    keyword: '산책'
  });

  const response = await fetch(`/api/course/calendar?${params}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });

  const data = await response.json();
  return data;
}
```

#### React 예시

```jsx
import { useState, useEffect } from 'react';

function CalendarView() {
  const [sessionDates, setSessionDates] = useState([]);
  const [currentMonth, setCurrentMonth] = useState(new Date());

  useEffect(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth() + 1;
    const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
    const endDate = new Date(year, month, 0).toISOString().split('T')[0];

    fetch(`/api/course/calendar?startDate=${startDate}&endDate=${endDate}`, {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    })
      .then(res => res.json())
      .then(data => setSessionDates(data.sessionDates));
  }, [currentMonth]);

  return (
    <div>
      {/* 달력 렌더링 */}
      {sessionDates.map(({ sessionDate, sessionCount }) => (
        <div key={sessionDate}>
          {sessionDate}: {sessionCount}개 세션
        </div>
      ))}
    </div>
  );
}
```

---

## 🎯 API 2: 특정 날짜의 코스 목록 조회

### 기본 정보

```
GET /api/course/calendar/courses
```

**용도**: 사용자가 달력에서 선택한 날짜의 코스 목록을 조회합니다.  
**사용 시점**: 달력에서 날짜를 클릭했을 때 사용  
**응답 형식**: 기존 `/api/course/search`와 **동일** (CourseSearchResponse)

### 요청

#### Headers
```http
Authorization: Bearer {access_token}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `date` | string | ✅ | 조회할 날짜 (yyyy-MM-dd) | `2024-01-15` |
| `keyword` | string | ❌ | 검색 키워드 | `기초` |
| `lessonForm` | string | ❌ | 수업 형태<br/>`WALK`, `GROUP`, `PRIVATE` | `WALK` |

### 응답

#### 성공 응답 (200 OK)

> ⚠️ **중요**: 이 응답은 기존 코스 검색 API(`/api/course/search`)와 **완전히 동일한 형식**입니다!

```json
{
  "courses": [
    {
      "courseId": 101,
      "trainerId": 5,
      "trainerName": "김훈련",
      "title": "강아지 기초 산책 훈련",
      "description": "산책 예절과 리드줄 훈련을 배웁니다",
      "tags": "산책,기초,예절",
      "mainImage": "https://s3.amazonaws.com/presigned-url...",
      "type": "MULTI",
      "lessonForm": "WALK",
      "status": "SCHEDULED",
      "difficulty": "BEGINNER",
      "isFree": false,
      "location": "서울시 강남구",
      "schedule": "매주 월,수,금 10:00",
      "dogSize": "ALL",
      "session": {
        "sessionId": 201,
        "startTime": "2024-01-15T10:00:00",
        "endTime": "2024-01-15T11:00:00",
        "locationDetail": "강남역 3번 출구 앞",
        "maxStudents": 5,
        "price": 50000
      }
    },
    {
      "courseId": 102,
      "trainerId": 8,
      "trainerName": "박트레이너",
      "title": "소형견 사회화 훈련",
      "description": "다른 강아지들과 어울리는 법을 배웁니다",
      "tags": "사회화,소형견,그룹",
      "mainImage": "https://s3.amazonaws.com/presigned-url...",
      "type": "ONCE",
      "lessonForm": "GROUP",
      "status": "SCHEDULED",
      "difficulty": "BEGINNER",
      "isFree": false,
      "location": "서울시 강남구",
      "schedule": "1회성 수업",
      "dogSize": "SMALL",
      "session": {
        "sessionId": 202,
        "startTime": "2024-01-15T14:00:00",
        "endTime": "2024-01-15T15:30:00",
        "locationDetail": "도곡공원",
        "maxStudents": 8,
        "price": 30000
      }
    }
  ],
  "hasMore": false,
  "lastCourseId": null,
  "size": 2
}
```

#### 응답 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `courses` | array | 코스 목록 |
| `courses[].courseId` | number | 코스 ID |
| `courses[].trainerId` | number | 훈련사 ID |
| `courses[].trainerName` | string | 훈련사 이름 |
| `courses[].title` | string | 코스 제목 |
| `courses[].description` | string | 코스 설명 |
| `courses[].tags` | string | 태그 (쉼표 구분) |
| `courses[].mainImage` | string | 메인 이미지 URL (S3 Presigned URL) |
| `courses[].type` | string | 훈련 유형<br/>`ONCE`: 1회성<br/>`MULTI`: 다회차 |
| `courses[].lessonForm` | string | 수업 형태<br/>`WALK`, `GROUP`, `PRIVATE` |
| `courses[].status` | string | 상태<br/>`SCHEDULED`, `CANCELLED`, `DONE` |
| `courses[].difficulty` | string | 난이도<br/>`BEGINNER`, `INTERMEDIATE`, `ADVANCED` |
| `courses[].isFree` | boolean | 무료 여부 |
| `courses[].location` | string | 위치 (시/도) |
| `courses[].schedule` | string | 일정 정보 |
| `courses[].dogSize` | string | 대상 강아지 크기<br/>`SMALL`, `MEDIUM`, `LARGE`, `ALL` |
| `courses[].session` | object | 세션 정보 (해당 날짜의 세션) |
| `courses[].session.sessionId` | number | 세션 ID |
| `courses[].session.startTime` | string | 시작 시간 (ISO 8601) |
| `courses[].session.endTime` | string | 종료 시간 (ISO 8601) |
| `courses[].session.locationDetail` | string | 상세 위치 |
| `courses[].session.maxStudents` | number | 최대 수강생 수 |
| `courses[].session.price` | number | 가격 (원) |
| `hasMore` | boolean | 다음 페이지 존재 여부<br/>(날짜별 조회는 항상 `false`) |
| `lastCourseId` | number \| null | 마지막 코스 ID<br/>(날짜별 조회는 항상 `null`) |
| `size` | number | 조회된 코스 수 |

#### 에러 응답

**400 Bad Request** - 잘못된 수업 형태
```json
{
  "status": 400,
  "message": "훈련 형태가 유효하지 않습니다. 허용된 값: WALK, GROUP, PRIVATE"
}
```

### 사용 예시

#### JavaScript (Fetch API)

```javascript
// 2024년 1월 15일의 코스 목록 조회
async function loadCoursesForDate(date) {
  const params = new URLSearchParams({
    date: date  // '2024-01-15'
  });

  const response = await fetch(`/api/course/calendar/courses?${params}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });

  const data = await response.json();
  
  // 기존 코스 리스트 컴포넌트와 동일하게 렌더링
  renderCourseList(data.courses);
}

// 달력에서 날짜 클릭 이벤트
function onCalendarDateClick(clickedDate) {
  loadCoursesForDate(clickedDate);
}
```

#### JavaScript (필터링 포함)

```javascript
// 필터링과 함께 조회
async function loadCoursesWithFilter(date, keyword, lessonForm) {
  const params = new URLSearchParams({ date });
  
  if (keyword) params.append('keyword', keyword);
  if (lessonForm) params.append('lessonForm', lessonForm);

  const response = await fetch(`/api/course/calendar/courses?${params}`, {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });

  return await response.json();
}

// 사용 예시
const data = await loadCoursesWithFilter('2024-01-15', '산책', 'WALK');
```

#### React 예시

```jsx
import { useState } from 'react';
import CourseList from './CourseList'; // 기존 컴포넌트 재사용!

function CalendarPage() {
  const [selectedDate, setSelectedDate] = useState(null);
  const [courses, setCourses] = useState([]);

  // 달력에서 날짜 클릭 시
  const handleDateClick = async (date) => {
    setSelectedDate(date);
    
    const response = await fetch(
      `/api/course/calendar/courses?date=${date}`,
      {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      }
    );
    
    const data = await response.json();
    setCourses(data.courses);
  };

  return (
    <div>
      <Calendar onDateClick={handleDateClick} />
      
      {selectedDate && (
        <div>
          <h2>{selectedDate}의 훈련 과정</h2>
          {/* 기존 코스 리스트 컴포넌트 재사용 */}
          <CourseList courses={courses} />
        </div>
      )}
    </div>
  );
}
```

---

## 🔄 완전한 플로우 예시

### 1. 페이지 로드 시

```javascript
// 현재 월의 달력 데이터 로드
async function initCalendar() {
  const today = new Date();
  const year = today.getFullYear();
  const month = (today.getMonth() + 1).toString().padStart(2, '0');
  
  const startDate = `${year}-${month}-01`;
  const lastDay = new Date(year, today.getMonth() + 1, 0).getDate();
  const endDate = `${year}-${month}-${lastDay.toString().padStart(2, '0')}`;

  // API 1: 달력 조회
  const response = await fetch(
    `/api/course/calendar?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    }
  );

  const { sessionDates } = await response.json();
  
  // 달력에 세션이 있는 날짜 표시
  renderCalendar(sessionDates);
}
```

### 2. 날짜 클릭 시

```javascript
// 사용자가 달력에서 날짜 클릭
async function handleDateClick(clickedDate) {
  // API 2: 특정 날짜의 코스 목록 조회
  const response = await fetch(
    `/api/course/calendar/courses?date=${clickedDate}`,
    {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    }
  );

  const { courses } = await response.json();
  
  // 코스 리스트 표시 (기존 컴포넌트 재사용)
  displayCourseList(courses);
}
```

### 3. 필터 적용

```javascript
// 필터 상태
const [filters, setFilters] = useState({
  keyword: '',
  lessonForm: ''
});

// 필터 변경 시 달력 및 코스 목록 갱신
async function applyFilters() {
  // 1. 달력 갱신
  const params = new URLSearchParams({
    startDate: currentStartDate,
    endDate: currentEndDate,
    ...(filters.keyword && { keyword: filters.keyword }),
    ...(filters.lessonForm && { lessonForm: filters.lessonForm })
  });

  const calendarResponse = await fetch(
    `/api/course/calendar?${params}`,
    {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    }
  );
  const calendarData = await calendarResponse.json();
  updateCalendar(calendarData.sessionDates);

  // 2. 선택된 날짜가 있으면 코스 목록도 갱신
  if (selectedDate) {
    const coursesResponse = await fetch(
      `/api/course/calendar/courses?${params}&date=${selectedDate}`,
      {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      }
    );
    const coursesData = await coursesResponse.json();
    updateCourseList(coursesData.courses);
  }
}
```

---

## 📱 UI/UX 권장사항

### 달력 표시

```javascript
// sessionCount에 따라 다르게 표시
function renderCalendarDate(date, sessionCount) {
  if (sessionCount === 0) {
    // 세션 없음: 기본 스타일
    return <div className="calendar-date">{date}</div>;
  } else if (sessionCount <= 2) {
    // 1-2개: 점 표시
    return (
      <div className="calendar-date has-session">
        {date}
        <span className="session-dot"></span>
      </div>
    );
  } else {
    // 3개 이상: 숫자 배지 표시
    return (
      <div className="calendar-date has-many-sessions">
        {date}
        <span className="session-badge">{sessionCount}</span>
      </div>
    );
  }
}
```

### 코스 리스트 표시

```javascript
// 시간순으로 정렬 (이미 API에서 정렬되어 옴)
function CourseItem({ course }) {
  const { session } = course;
  const startTime = new Date(session.startTime);
  
  return (
    <div className="course-item">
      <img src={course.mainImage} alt={course.title} />
      <h3>{course.title}</h3>
      <p className="trainer">{course.trainerName}</p>
      <p className="time">
        {startTime.getHours()}:{startTime.getMinutes().toString().padStart(2, '0')}
        ~
        {/* endTime도 동일하게 포맷 */}
      </p>
      <p className="price">
        {session.price.toLocaleString()}원
      </p>
      <p className="capacity">
        정원: {session.maxStudents}명
      </p>
    </div>
  );
}
```

---

## 🔐 권한 및 필터링

### 자동 필터링

API는 사용자의 역할에 따라 자동으로 필터링됩니다:

- **일반 사용자 (USER)**: 자신이 속한 훈련사의 과정만 조회
- **훈련사 (TRAINER)**: 자신이 등록한 과정만 조회

> 💡 **프론트엔드에서 별도 처리 불필요**  
> 백엔드에서 자동으로 필터링되므로 추가 처리가 필요 없습니다.

---

## 🐛 에러 처리

### 공통 에러 핸들링

```javascript
async function fetchWithErrorHandling(url, options) {
  try {
    const response = await fetch(url, options);
    
    if (!response.ok) {
      const error = await response.json();
      
      switch (response.status) {
        case 400:
          alert(error.message);
          break;
        case 401:
          // 로그인 페이지로 리다이렉트
          window.location.href = '/login';
          break;
        case 403:
          alert('권한이 없습니다.');
          break;
        case 404:
          alert('데이터를 찾을 수 없습니다.');
          break;
        default:
          alert('오류가 발생했습니다.');
      }
      
      throw new Error(error.message);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API 호출 실패:', error);
    throw error;
  }
}
```

---

## 💡 자주 묻는 질문 (FAQ)

### Q1. 기존 코스 검색 API와 응답 형식이 같나요?

✅ **네, 완전히 동일합니다.**

`/api/course/calendar/courses`의 응답은 `/api/course/search`와 동일한 `CourseSearchResponse` 형식입니다. 기존 코스 리스트 컴포넌트를 그대로 재사용할 수 있습니다.

### Q2. hasMore가 항상 false인 이유는?

날짜별 조회는 **무한 스크롤이 없기 때문**입니다. 특정 날짜의 모든 코스를 한 번에 조회합니다.

### Q3. mainImage URL이 바로 사용 가능한가요?

✅ **네, Presigned URL로 변환되어 제공됩니다.**

별도 변환 없이 `<img src={course.mainImage} />`로 바로 사용 가능합니다. (유효기간 있음)

### Q4. 필터를 적용하면 어떻게 되나요?

필터(`keyword`, `lessonForm`)는 **두 API 모두**에 적용됩니다:
- 달력: 필터링된 코스의 세션만 표시
- 코스 목록: 필터링된 코스만 표시

### Q5. 날짜 형식은 무엇인가요?

모든 날짜는 **`yyyy-MM-dd` 형식** (ISO 8601)을 사용합니다.
- 예: `2024-01-15`, `2024-12-31`

---

## 📊 응답 크기 및 성능

### 예상 응답 크기

- **달력 API**: 약 500B ~ 2KB (한 달 기준)
- **코스 목록 API**: 약 3KB ~ 30KB (코스 10개 기준)

### 권장사항

- 달력 데이터는 **월 단위로 캐싱** 추천
- 코스 목록은 **날짜별로 캐싱** 추천
- 필터 변경 시에만 API 재호출

---

## 🎨 UI 컴포넌트 예시

### 완전한 React 컴포넌트

```jsx
import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar'; // 예시
import 'react-calendar/dist/Calendar.css';

function CourseCalendarPage() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [sessionDates, setSessionDates] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [courses, setCourses] = useState([]);
  const [filters, setFilters] = useState({
    keyword: '',
    lessonForm: ''
  });

  // 1. 달력 데이터 로드
  useEffect(() => {
    loadCalendarData();
  }, [currentMonth, filters]);

  async function loadCalendarData() {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth() + 1;
    const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${month.toString().padStart(2, '0')}-${lastDay.toString().padStart(2, '0')}`;

    const params = new URLSearchParams({
      startDate,
      endDate,
      ...(filters.keyword && { keyword: filters.keyword }),
      ...(filters.lessonForm && { lessonForm: filters.lessonForm })
    });

    const response = await fetch(`/api/course/calendar?${params}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });

    const data = await response.json();
    setSessionDates(data.sessionDates);
  }

  // 2. 날짜 클릭 시 코스 목록 로드
  async function handleDateClick(date) {
    const dateStr = date.toISOString().split('T')[0];
    setSelectedDate(dateStr);

    const params = new URLSearchParams({
      date: dateStr,
      ...(filters.keyword && { keyword: filters.keyword }),
      ...(filters.lessonForm && { lessonForm: filters.lessonForm })
    });

    const response = await fetch(`/api/course/calendar/courses?${params}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });

    const data = await response.json();
    setCourses(data.courses);
  }

  // 3. 특정 날짜에 세션이 있는지 확인
  function hasSessionOnDate(date) {
    const dateStr = date.toISOString().split('T')[0];
    return sessionDates.find(sd => sd.sessionDate === dateStr);
  }

  return (
    <div className="course-calendar-page">
      {/* 필터 */}
      <div className="filters">
        <input
          type="text"
          placeholder="검색..."
          value={filters.keyword}
          onChange={(e) => setFilters({...filters, keyword: e.target.value})}
        />
        <select
          value={filters.lessonForm}
          onChange={(e) => setFilters({...filters, lessonForm: e.target.value})}
        >
          <option value="">모든 수업</option>
          <option value="WALK">산책 훈련</option>
          <option value="GROUP">그룹 훈련</option>
          <option value="PRIVATE">개인 훈련</option>
        </select>
      </div>

      {/* 달력 */}
      <Calendar
        value={currentMonth}
        onActiveStartDateChange={({ activeStartDate }) => setCurrentMonth(activeStartDate)}
        onClickDay={handleDateClick}
        tileContent={({ date }) => {
          const session = hasSessionOnDate(date);
          if (session) {
            return (
              <div className="session-indicator">
                {session.sessionCount > 2 ? (
                  <span className="badge">{session.sessionCount}</span>
                ) : (
                  <span className="dot"></span>
                )}
              </div>
            );
          }
          return null;
        }}
      />

      {/* 코스 목록 */}
      {selectedDate && (
        <div className="course-list">
          <h2>{selectedDate}의 훈련 과정</h2>
          {courses.length === 0 ? (
            <p>해당 날짜에 훈련 과정이 없습니다.</p>
          ) : (
            courses.map(course => (
              <div key={course.courseId} className="course-card">
                <img src={course.mainImage} alt={course.title} />
                <h3>{course.title}</h3>
                <p>{course.trainerName}</p>
                <p>
                  {new Date(course.session.startTime).toLocaleTimeString('ko-KR', {
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </p>
                <p>{course.session.price.toLocaleString()}원</p>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default CourseCalendarPage;
```

---

## 🚀 빠른 시작 체크리스트

- [ ] Access Token 준비 (Authorization 헤더에 사용)
- [ ] 달력 라이브러리 설치 (선택사항)
- [ ] API 1 호출하여 달력에 세션 날짜 표시
- [ ] 날짜 클릭 이벤트 구현
- [ ] API 2 호출하여 코스 목록 표시
- [ ] 기존 코스 리스트 컴포넌트 재사용
- [ ] 필터링 UI 추가 (선택사항)
- [ ] 에러 처리 구현

---

## 📞 문의

API 관련 문의사항이나 버그 리포트는 백엔드 팀에게 연락주세요.

**최종 업데이트**: 2024-12-22  
**API 버전**: v1

