# 🎯 프론트엔드 개발자를 위한 반려견 통계 API 구현 가이드

> **백엔드 업데이트 완료!** UUID 기반 수강 이력 그룹화가 백엔드에서 처리됩니다.  
> **작성일**: 2025-12-23  
> **대상**: Next.js/React 프론트엔드 개발자

---

## 📋 목차

1. [변경 사항 요약](#변경-사항-요약)
2. [API 응답 구조](#api-응답-구조)
3. [TypeScript 타입 정의](#typescript-타입-정의)
4. [구현 예시](#구현-예시)
5. [마이그레이션 가이드](#마이그레이션-가이드)
6. [체크리스트](#체크리스트)

---

## 변경 사항 요약

### 🎯 무엇이 바뀌었나요?

**Before (기존)**:
- 같은 훈련 과정을 여러 번 수강하면 각각 별도 항목으로 표시
- 프론트에서 그룹화 처리 필요
- 전체 출석률 계산 필요

**After (신규 - 백엔드에서 처리됨)**:
- ✅ 같은 UUID를 가진 과정들이 **백엔드에서 이미 그룹화**됨
- ✅ **수강 횟수** 자동 계산됨
- ✅ **전체 평균 출석률** 자동 계산됨
- ✅ **수강 이력** 자동 생성됨 (1차, 2차, 3차...)

### 📊 예시

```
Before: 
- 기초 훈련 (2024년 1월)
- 기초 훈련 (2024년 7월)  
- 기초 훈련 심화 (2024년 12월)

After:
기초 훈련 [3회 수강] 📊 평균 80%
  ├─ 1차 (2024.01): 기초 훈련 - 80%
  ├─ 2차 (2024.07): 기초 훈련 - 90%
  └─ 3차 (2024.12): 기초 훈련 심화 - 70%
```

---

## API 응답 구조

### 🔗 Endpoint
```
GET /api/trainer/user/dogs/{dogId}
```

### 📦 응답 예시

#### 단일 수강인 경우
```json
{
  "multiCourses": [{
    "tags": "기초,사회화,복종",
    "courses": [{
      "courseId": 1,
      "title": "강아지 기초 훈련 4주 코스",
      "tags": "기초,사회화,복종",
      "location": "서울시 강남구",
      "difficulty": "BEGINNER",
      "enrollmentCount": 1,  // ⭐ 1회만 수강
      "totalSessions": 10,
      "attendedSessions": 8,
      "attendanceRate": 80.0,
      "enrollmentHistory": null,  // ⭐ 단일 수강이므로 null
      "sessions": [
        {
          "sessionId": 1,
          "sessionNo": 1,
          "sessionDate": "2026-01-10",
          "startTime": "14:00",
          "endTime": "15:30",
          "locationDetail": "강남센터 1층",
          "attendanceStatus": "ATTENDED"
        }
        // ... 나머지 세션들
      ]
    }]
  }]
}
```

#### 여러 번 수강한 경우 (⭐ 중요!)
```json
{
  "multiCourses": [{
    "tags": "기초,사회화,복종",
    "courses": [{
      "courseId": 1,
      "title": "강아지 기초 훈련 4주 코스",
      "tags": "기초,사회화,복종",
      "location": "서울시 강남구",
      "difficulty": "BEGINNER",
      
      // ⭐ 새로 추가된 필드들
      "enrollmentCount": 3,  // 총 3회 수강
      "totalSessions": 30,    // 전체 합산 (10 + 10 + 10)
      "attendedSessions": 24, // 전체 합산 (8 + 9 + 7)
      "attendanceRate": 80.0, // 전체 평균 (24/30)
      
      "sessions": [],  // ⭐ 비어있음! (이력에 포함됨)
      
      // ⭐ 수강 이력 (핵심!)
      "enrollmentHistory": [
        {
          "enrollmentNumber": 1,  // 1차 수강
          "courseId": 1,
          "title": "강아지 기초 훈련 4주 코스",
          "description": "4주 동안 진행되는...",
          "startDate": "2024-01-10",
          "endDate": "2024-02-10",
          "totalSessions": 10,
          "attendedSessions": 8,
          "attendanceRate": 80.0,
          "sessions": [
            {
              "sessionId": 1,
              "sessionNo": 1,
              "sessionDate": "2024-01-10",
              "startTime": "14:00",
              "endTime": "15:30",
              "locationDetail": "강남센터 1층",
              "attendanceStatus": "ATTENDED"
            }
            // ... 10개 세션
          ]
        },
        {
          "enrollmentNumber": 2,  // 2차 수강
          "courseId": 5,
          "title": "강아지 기초 훈련 4주 코스 (2024 겨울)",  // ⭐ 제목 차이!
          "startDate": "2024-07-10",
          "endDate": "2024-08-10",
          "totalSessions": 10,
          "attendedSessions": 9,
          "attendanceRate": 90.0,
          "sessions": [...]
        },
        {
          "enrollmentNumber": 3,  // 3차 수강
          "courseId": 9,
          "title": "강아지 기초 훈련 4주 코스 - 심화",  // ⭐ 제목 차이!
          "startDate": "2024-12-10",
          "endDate": "2025-01-10",
          "totalSessions": 10,
          "attendedSessions": 7,
          "attendanceRate": 70.0,
          "sessions": [...]
        }
      ]
    }]
  }]
}
```

---

## TypeScript 타입 정의

### 📘 완전한 타입 (복사해서 사용하세요)

```typescript
// types/dog-stats.ts

export interface DogStatsResponse {
  dog: DogResponse;
  counselings: CounselingResponse[];
  stats: Stats;
  trainingApplications: TrainingSessionDto[];
  multiCourses: MultiCourseCategoryResponse[];
}

// ⭐ 단회차 훈련 (새로 추가)
export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  tags: string;
  type: 'SINGLE';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';  // 난이도
  sessionId: number;
  sessionDate: string;         // YYYY-MM-DD
  sessionStartTime: string;    // HH:mm:ss
  sessionEndTime: string;      // HH:mm:ss
  attendanceStatus: 'ATTENDED' | 'ABSENT' | null;  // 출석 상태
}

export interface MultiCourseCategoryResponse {
  tags: string;
  courses: MultiCourseGroupResponse[];
}

export interface MultiCourseGroupResponse {
  courseId: number;
  title: string;
  tags: string;
  description: string;
  location: string;
  type: 'MULTI';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  mainImage: string;
  
  // ⭐ 새로 추가된 필드
  enrollmentCount: number;  // 수강 횟수
  enrollmentHistory: EnrollmentHistory[] | null;  // 수강 이력 (단일 수강이면 null)
  
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: MultiSessionResponse[];  // 여러 수강이면 빈 배열
}

// ⭐ 새로 추가된 타입
export interface EnrollmentHistory {
  enrollmentNumber: number;  // 몇 차 수강 (1, 2, 3...)
  courseId: number;
  title: string;  // 과정별 미세한 차이
  description: string;
  startDate: string;  // YYYY-MM-DD
  endDate: string;    // YYYY-MM-DD
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: MultiSessionResponse[];
}

export interface MultiSessionResponse {
  sessionId: number;
  sessionNo: number;
  sessionDate: string;  // YYYY-MM-DD
  startTime: string;    // HH:mm
  endTime: string;      // HH:mm
  locationDetail: string;
  attendanceStatus: 'ATTENDED' | 'ABSENT' | null;
}
```

---

## 구현 예시

### 1️⃣ 코스 카드 컴포넌트

```typescript
// components/MultiCourseCard.tsx
'use client';

import { useState } from 'react';
import { MultiCourseGroupResponse } from '@/types/dog-stats';
import SessionTimeline from './SessionTimeline';

interface Props {
  course: MultiCourseGroupResponse;
}

export default function MultiCourseCard({ course }: Props) {
  const [isExpanded, setIsExpanded] = useState(false);

  const difficultyConfig = {
    BEGINNER: { label: '초급', color: 'bg-green-100 text-green-800' },
    INTERMEDIATE: { label: '중급', color: 'bg-yellow-100 text-yellow-800' },
    ADVANCED: { label: '고급', color: 'bg-red-100 text-red-800' },
  };

  const difficulty = difficultyConfig[course.difficulty];

  // ⭐ 단일 수강 vs 여러 수강 판단
  const isMultipleEnrollments = course.enrollmentCount > 1;

  return (
    <div className="border rounded-lg overflow-hidden">
      {/* 헤더 */}
      <div
        className="bg-gray-50 p-4 cursor-pointer hover:bg-gray-100"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-lg font-bold">{course.title}</h3>
              
              {/* ⭐ 수강 횟수 뱃지 (여러 수강인 경우만) */}
              {isMultipleEnrollments && (
                <span className="bg-blue-500 text-white px-3 py-1 rounded-full text-sm font-bold">
                  {course.enrollmentCount}회 수강
                </span>
              )}
              
              <span className={`px-2 py-1 rounded text-xs font-semibold ${difficulty.color}`}>
                {difficulty.label}
              </span>
            </div>
            
            <p className="text-sm text-gray-600 mb-3">{course.description}</p>
            
            <div className="flex items-center gap-4 text-sm">
              <span>📍 {course.location}</span>
              <span>📅 총 {course.totalSessions}회</span>
              <span>✅ 출석 {course.attendedSessions}회</span>
              <span className="font-semibold text-blue-600">
                출석률: {course.attendanceRate.toFixed(1)}%
              </span>
            </div>
          </div>
          
          <button className="text-2xl ml-4">
            {isExpanded ? '🔼' : '🔽'}
          </button>
        </div>

        {/* 출석률 프로그레스 바 */}
        <div className="mt-3 bg-gray-200 rounded-full h-2">
          <div
            className="bg-blue-500 h-full rounded-full"
            style={{ width: `${Math.min(course.attendanceRate, 100)}%` }}
          />
        </div>
      </div>

      {/* 상세 내용 (펼쳤을 때) */}
      {isExpanded && (
        <div className="p-4 bg-white border-t">
          {/* ⭐ 여러 수강인 경우: 수강 이력 표시 */}
          {isMultipleEnrollments && course.enrollmentHistory ? (
            <div className="space-y-4">
              <h4 className="font-semibold text-gray-800">
                📚 수강 이력 ({course.enrollmentCount}회)
              </h4>
              
              {course.enrollmentHistory.map((enrollment) => (
                <EnrollmentHistoryItem 
                  key={enrollment.enrollmentNumber} 
                  enrollment={enrollment} 
                />
              ))}
            </div>
          ) : (
            /* ⭐ 단일 수강인 경우: 바로 세션 표시 */
            <SessionTimeline sessions={course.sessions} />
          )}
        </div>
      )}
    </div>
  );
}
```

### 2️⃣ 수강 이력 아이템 (새로 추가)

```typescript
// components/EnrollmentHistoryItem.tsx
'use client';

import { useState } from 'react';
import { EnrollmentHistory } from '@/types/dog-stats';
import SessionTimeline from './SessionTimeline';

interface Props {
  enrollment: EnrollmentHistory;
}

export default function EnrollmentHistoryItem({ enrollment }: Props) {
  const [showSessions, setShowSessions] = useState(false);

  return (
    <div className="border-l-4 border-blue-400 pl-4 py-3 bg-gray-50 rounded-r">
      {/* 수강 차수 */}
      <div className="flex items-center gap-2 mb-2">
        <span className="bg-blue-600 text-white px-2 py-1 rounded text-sm font-bold">
          {enrollment.enrollmentNumber}차 수강
        </span>
        <span className="text-sm text-gray-600">
          {enrollment.startDate} ~ {enrollment.endDate}
        </span>
      </div>
      
      {/* 제목 (⭐ 과정별 차이) */}
      <p className="font-semibold text-gray-900 mb-1">
        {enrollment.title}
      </p>
      
      {/* 통계 */}
      <div className="flex items-center gap-4 text-sm mb-2">
        <span>
          📊 {enrollment.attendedSessions}/{enrollment.totalSessions}회 출석
        </span>
        <span className="font-semibold text-green-600">
          출석률: {enrollment.attendanceRate.toFixed(1)}%
        </span>
      </div>

      {/* 세션 펼치기 버튼 */}
      <button
        onClick={() => setShowSessions(!showSessions)}
        className="text-sm text-blue-600 hover:text-blue-800 font-medium"
      >
        {showSessions ? '🔼' : '🔽'} 
        세션 상세 보기 ({enrollment.sessions.length}회차)
      </button>

      {/* 세션 타임라인 */}
      {showSessions && (
        <div className="mt-3 pl-4 border-l-2 border-gray-200">
          <SessionTimeline sessions={enrollment.sessions} />
        </div>
      )}
    </div>
  );
}
```

### 3️⃣ 세션 타임라인 (기존 재사용)

```typescript
// components/SessionTimeline.tsx
import { MultiSessionResponse } from '@/types/dog-stats';

interface Props {
  sessions: MultiSessionResponse[];
}

export default function SessionTimeline({ sessions }: Props) {
  const getStatusInfo = (status: string | null) => {
    if (status === 'ATTENDED') {
      return { label: '출석', color: 'bg-green-500', bgColor: 'bg-green-100' };
    }
    if (status === 'ABSENT') {
      return { label: '결석', color: 'bg-red-500', bgColor: 'bg-red-100' };
    }
    return { label: '예정', color: 'bg-gray-300', bgColor: 'bg-gray-100' };
  };

  return (
    <div className="space-y-3">
      {sessions.map((session, index) => {
        const statusInfo = getStatusInfo(session.attendanceStatus);
        
        return (
          <div key={session.sessionId} className="flex items-start gap-4">
            {/* 타임라인 점 */}
            <div className="flex flex-col items-center pt-1">
              <div className={`w-4 h-4 rounded-full ${statusInfo.color}`} />
              {index < sessions.length - 1 && (
                <div className="w-0.5 bg-gray-300 h-12" />
              )}
            </div>

            {/* 세션 정보 */}
            <div className="flex-1 pb-4">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-semibold">{session.sessionNo}회차</span>
                <span className={`px-2 py-0.5 rounded text-xs font-semibold ${statusInfo.bgColor}`}>
                  {statusInfo.label}
                </span>
              </div>
              <p className="text-sm text-gray-600">
                📅 {new Date(session.sessionDate).toLocaleDateString('ko-KR')}
              </p>
              <p className="text-sm text-gray-600">
                ⏰ {session.startTime} ~ {session.endTime}
              </p>
              <p className="text-sm text-gray-600">
                📍 {session.locationDetail}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
```

---

## 마이그레이션 가이드

### ❌ 제거해야 할 것들

#### 1. 그룹화 유틸리티 함수 (더 이상 불필요)
```typescript
// ❌ 삭제: utils/groupCoursesByTags.ts
// 백엔드에서 이미 그룹화됨!
```

#### 2. 프론트 그룹화 로직
```typescript
// ❌ 삭제
const groupedCourses = useMemo(
  () => groupCoursesByTags(category.courses),
  [category.courses]
);
```

### ✅ 추가해야 할 것들

#### 1. 타입 정의 업데이트
```typescript
// types/dog-stats.ts에 추가

export interface EnrollmentHistory {
  enrollmentNumber: number;
  courseId: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: MultiSessionResponse[];
}
```

#### 2. MultiCourseGroupResponse 타입 업데이트
```typescript
export interface MultiCourseGroupResponse {
  // ...existing fields...
  
  // ⭐ 추가
  enrollmentCount: number;
  enrollmentHistory: EnrollmentHistory[] | null;
}
```

#### 3. 새 컴포넌트 생성
```bash
components/EnrollmentHistoryItem.tsx  # 새로 생성
```

#### 4. 기존 컴포넌트 수정
```typescript
// components/MultiCourseCard.tsx
// - enrollmentCount 체크 추가
// - enrollmentHistory 렌더링 추가
// - 단일/여러 수강 분기 처리
```

---

## 체크리스트

### 📋 개발 전 확인
- [ ] 백엔드 API 배포 완료 확인
- [ ] API 응답 테스트 (Postman/Insomnia)
- [ ] TypeScript 타입 정의 복사

### 📋 구현
- [ ] `types/dog-stats.ts` 업데이트
  - [ ] `EnrollmentHistory` 인터페이스 추가
  - [ ] `MultiCourseGroupResponse`에 필드 추가
- [ ] `components/EnrollmentHistoryItem.tsx` 생성
- [ ] `components/MultiCourseCard.tsx` 수정
  - [ ] `enrollmentCount` 체크 로직 추가
  - [ ] `enrollmentHistory` 렌더링 추가
  - [ ] 단일/여러 수강 분기 처리
- [ ] 기존 그룹화 로직 제거
  - [ ] `utils/groupCoursesByTags.ts` 삭제
  - [ ] 관련 import 제거

### 📋 테스트
- [ ] 단일 수강 케이스 테스트
  - [ ] `enrollmentCount: 1`
  - [ ] `enrollmentHistory: null`
  - [ ] 세션 목록 정상 표시
- [ ] 여러 수강 케이스 테스트
  - [ ] `enrollmentCount: 2+`
  - [ ] 수강 횟수 뱃지 표시
  - [ ] 수강 이력 펼치기/접기
  - [ ] 각 수강의 title 차이 확인
  - [ ] 전체 평균 출석률 확인
- [ ] 빈 데이터 케이스
  - [ ] `multiCourses: []`
  - [ ] 빈 상태 UI 표시

### 📋 배포 전
- [ ] TypeScript 컴파일 에러 없음
- [ ] 브라우저 콘솔 에러 없음
- [ ] 반응형 디자인 확인
- [ ] 성능 최적화 (useMemo/useCallback)

---

## ⚠️ 주의사항

### 1. Null 체크 필수
```typescript
// ⭐ 단일 수강이면 enrollmentHistory가 null
{course.enrollmentHistory ? (
  course.enrollmentHistory.map(...)
) : (
  <SessionTimeline sessions={course.sessions} />
)}
```

### 2. Sessions 위치 변경
```typescript
// ❌ 잘못된 코드
<SessionTimeline sessions={course.sessions} />  // 여러 수강이면 빈 배열!

// ✅ 올바른 코드
{course.enrollmentHistory ? (
  // 여러 수강: enrollmentHistory[].sessions 사용
  course.enrollmentHistory.map(enrollment => (
    <SessionTimeline sessions={enrollment.sessions} />
  ))
) : (
  // 단일 수강: course.sessions 사용
  <SessionTimeline sessions={course.sessions} />
)}
```

### 3. EnrollmentCount 조건부 렌더링
```typescript
// 1회 수강이면 뱃지 안 보이게
{course.enrollmentCount > 1 && (
  <span>{course.enrollmentCount}회 수강</span>
)}
```

---

## 🆘 문제 해결

### Q1. "enrollmentCount가 undefined"
**원인**: 백엔드 API가 아직 배포 안 됨  
**해결**: 백엔드 팀에 배포 여부 확인

### Q2. "세션이 안 보여요"
**원인**: 여러 수강인데 `course.sessions` 사용  
**해결**: `enrollment.sessions` 사용

```typescript
// ❌
<SessionTimeline sessions={course.sessions} />

// ✅
{course.enrollmentHistory.map(enrollment => (
  <SessionTimeline sessions={enrollment.sessions} />
))}
```

### Q3. "타입 에러가 나요"
**원인**: 타입 정의 누락  
**해결**: `EnrollmentHistory` 인터페이스 추가

---

## 📞 연락처

**백엔드 담당**: [백엔드 팀]  
**API 문서**: `BACKEND_UUID_GROUPING_IMPLEMENTATION.md`  
**상세 가이드**: `COURSE_GROUPING_BY_UUID_GUIDE.md`

---

## ✅ 완료!

이 문서대로 구현하면:
- ✅ 수강 횟수 뱃지 표시
- ✅ 전체 평균 출석률 표시
- ✅ 수강 이력 타임라인
- ✅ 과정별 차이 명확히 표시
- ✅ 깔끔한 UI/UX

**좋은 코딩 되세요!** 🚀

---

**작성일**: 2025-12-23  
**버전**: v1.0  
**업데이트**: 백엔드 API 배포 후

