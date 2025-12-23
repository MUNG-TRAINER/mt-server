# 🐕 반려견 통계 페이지 API 상세 문서

> **프론트엔드 개발 완벽 가이드**  
> 반려견의 훈련 이력, 상담 기록, 출석률 등을 한 페이지에서 확인할 수 있는 통계 API

---

## 📋 목차
1. [API 개요](#api-개요)
2. [응답 데이터 구조 완전 분석](#응답-데이터-구조-완전-분석)
3. [실제 응답 예시](#실제-응답-예시)
4. [TypeScript 인터페이스](#typescript-인터페이스)
5. [프론트엔드 구현 예시](#프론트엔드-구현-예시)
6. [UI 컴포넌트 설계](#ui-컴포넌트-설계)
7. [주의사항 및 트러블슈팅](#주의사항-및-트러블슈팅)

---

## API 개요

### 📌 기본 정보
- **Endpoint**: `GET /api/trainer/user/dogs/{dogId}`
- **설명**: 특정 반려견의 전체 훈련 이력, 상담 기록, 통계 정보를 조회합니다.
- **인증**: 필수 (JWT Token)
- **용도**: 반려견 상세 페이지, 훈련 이력 대시보드

### 🎯 API가 제공하는 정보
1. **반려견 기본 정보** (프로필 이미지 포함)
2. **상담 기록** (훈련사가 작성한 상담 내역)
3. **통계 요약** (총 신청 횟수, 총 출석 횟수)
4. **단회차 훈련 이력** (1회성 훈련 세션 목록)
5. **다회차 훈련 이력** (태그별로 그룹화된 코스 및 세션)

### 📝 Request

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| dogId | Long | ✅ | 조회할 반려견의 고유 ID |

#### Headers
```http
Authorization: Bearer {JWT_TOKEN}
```

#### Request Example
```typescript
const getDogStats = async (dogId: number) => {
  const response = await fetch(`/api/trainer/user/dogs/${dogId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error('반려견 통계 조회 실패');
  }

  return response.json();
};
```

---

## 응답 데이터 구조 완전 분석

### 🔍 최상위 구조

```typescript
{
  dog: DogResponse,                              // 반려견 기본 정보
  counselings: CounselingResponse[],             // 상담 기록 배열
  stats: Stats,                                  // 통계 요약
  trainingApplications: TrainingSessionDto[],    // 단회차 훈련 목록
  multiCourses: MultiCourseCategoryResponse[]    // 다회차 훈련 (태그별 그룹)
}
```

---

### 1️⃣ **dog** - 반려견 기본 정보

```typescript
{
  "dogId": 1,
  "dogName": "멍멍이",
  "breed": "골든 리트리버",
  "age": 3,
  "gender": "MALE",
  "weight": 28.5,
  "profileImage": "https://mungtrainer-s3.s3.ap-northeast-2.amazonaws.com/...",
  "neutered": true,
  "registeredDate": "2024-01-10T09:00:00"
}
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| dogId | number | 반려견 고유 ID |
| dogName | string | 반려견 이름 |
| breed | string | 견종 |
| age | number | 나이 (년) |
| gender | string | 성별 (`MALE` / `FEMALE`) |
| weight | number | 체중 (kg) |
| profileImage | string \| null | 프로필 이미지 S3 Presigned URL (유효기간 15분) |
| neutered | boolean | 중성화 여부 |
| registeredDate | string | 등록일시 (ISO 8601) |

---

### 2️⃣ **counselings** - 상담 기록 배열

```typescript
[
  {
    "counselingId": 101,
    "dogId": 1,
    "content": "산책 시 다른 개를 보면 짖는 문제가 있어 집중 훈련 필요",
    "trainerId": 5,
    "isCompleted": true,
    "createdAt": "2024-11-01T10:30:00",
    "updatedAt": "2024-11-05T14:20:00"
  },
  {
    "counselingId": 102,
    "dogId": 1,
    "content": "기본 복종 훈련 진행 중. 앉아, 기다려 명령 잘 따름",
    "trainerId": 5,
    "isCompleted": false,
    "createdAt": "2024-12-01T09:00:00",
    "updatedAt": "2024-12-01T09:00:00"
  }
]
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| counselingId | number | 상담 고유 ID |
| dogId | number | 반려견 ID |
| content | string | 상담 내용 |
| trainerId | number | 작성한 훈련사 ID |
| isCompleted | boolean | 상담 완료 여부 |
| createdAt | string | 상담 생성일시 (ISO 8601) |
| updatedAt | string | 상담 수정일시 (ISO 8601) |

---

### 3️⃣ **stats** - 통계 요약 (전체)

```typescript
{
  "timesApplied": 8,      // 총 신청한 훈련 횟수 (단회차 + 다회차 전체)
  "attendedCount": 4      // 총 출석한 세션 수 (단회차 + 다회차 전체)
}
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| timesApplied | number | **단회차 + 다회차** 통합 총 신청 횟수 |
| attendedCount | number | **단회차 + 다회차** 통합 총 출석 횟수 |

> 💡 **Tip**: 전체 출석률 = `(attendedCount / timesApplied) * 100`

> 📝 **계산 방식** (2025-12-23 업데이트):
> - **단회차 통계**: 태그별로 그룹화된 신청/출석 횟수 합산
> - **다회차 통계**: 각 코스의 totalSessions/attendedSessions 합산
> - **최종 stats**: 단회차 + 다회차 통계를 모두 합산한 값
> 
> **예시**:
> - 단회차: 신청 5회, 출석 3회
> - 다회차: 신청 3회 (세션), 출석 1회
> - **stats**: timesApplied=8 (5+3), attendedCount=4 (3+1)
> - **전체 출석률**: 50% (4/8 * 100)

---

### 4️⃣ **trainingApplications** - 단회차 훈련 목록

단회차 훈련(type: `SINGLE`)에 신청한 모든 세션 목록입니다.

```typescript
[
  {
    "courseId": 201,
    "courseTitle": "기본 복종 훈련",
    "courseDescription": "앉아, 엎드려, 기다려 등 기본 명령어 훈련",
    "tags": "기본훈련",
    "type": "SINGLE",
    "sessionId": 301,
    "sessionDate": "2024-11-15",
    "sessionStartTime": "10:00:00",
    "sessionEndTime": "11:00:00"
  },
  {
    "courseId": 202,
    "courseTitle": "산책 훈련",
    "courseDescription": "줄 당기지 않고 걷기",
    "tags": "행동교정",
    "type": "SINGLE",
    "sessionId": 302,
    "sessionDate": "2024-11-20",
    "sessionStartTime": "14:00:00",
    "sessionEndTime": "15:00:00"
  }
]
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| courseId | number | 코스 ID |
| courseTitle | string | 코스 제목 |
| courseDescription | string | 코스 설명 |
| tags | string | 태그 (카테고리) |
| type | string | 코스 타입 (항상 `"SINGLE"`) |
| sessionId | number | 세션 ID |
| sessionDate | string | 세션 날짜 (YYYY-MM-DD) |
| sessionStartTime | string | 시작 시간 (HH:mm:ss) |
| sessionEndTime | string | 종료 시간 (HH:mm:ss) |

---

### 5️⃣ **multiCourses** - 다회차 훈련 (태그별 그룹)

⭐ **가장 복잡한 구조입니다!** 다회차 훈련을 태그별로 그룹화하고, 각 코스마다 세션 정보를 포함합니다.

#### 📐 구조: 3단계 중첩

```
multiCourses (배열)
  └─ MultiCourseCategoryResponse (태그별 그룹)
      ├─ tags: "기본훈련"
      └─ courses (배열)
          └─ MultiCourseGroupResponse (개별 코스)
              ├─ courseId, title, description...
              ├─ totalSessions: 10
              ├─ attendedSessions: 8
              ├─ attendanceRate: 80.0
              └─ sessions (배열)
                  └─ MultiSessionResponse (개별 세션)
                      ├─ sessionId, sessionNo
                      ├─ sessionDate, startTime, endTime
                      └─ attendanceStatus: "ATTENDED" / "ABSENT" / null
```

#### 실제 데이터 예시

```typescript
[
  {
    "tags": "기본훈련",
    "courses": [
      {
        "courseId": 101,
        "title": "퍼피 기초 훈련 과정",
        "tags": "기본훈련",
        "description": "강아지 시기에 배워야 할 기본 훈련",
        "location": "강남센터",
        "type": "MULTI",
        "difficulty": "BEGINNER",
        "mainImage": "https://s3.../course-101.jpg",
        "totalSessions": 10,
        "attendedSessions": 8,
        "attendanceRate": 80.0,
        "sessions": [
          {
            "sessionId": 1001,
            "sessionNo": 1,
            "sessionDate": "2024-11-01",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "locationDetail": "강남센터 1층 훈련장",
            "attendanceStatus": "ATTENDED"
          },
          {
            "sessionId": 1002,
            "sessionNo": 2,
            "sessionDate": "2024-11-08",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "locationDetail": "강남센터 1층 훈련장",
            "attendanceStatus": "ATTENDED"
          },
          {
            "sessionId": 1003,
            "sessionNo": 3,
            "sessionDate": "2024-11-15",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "locationDetail": "강남센터 1층 훈련장",
            "attendanceStatus": "ABSENT"
          },
          // ... 나머지 세션들
        ]
      }
    ]
  },
  {
    "tags": "행동교정",
    "courses": [
      {
        "courseId": 102,
        "title": "짖음 교정 집중 과정",
        "tags": "행동교정",
        "description": "과도한 짖음 문제 해결",
        "location": "서초센터",
        "type": "MULTI",
        "difficulty": "INTERMEDIATE",
        "mainImage": "https://s3.../course-102.jpg",
        "totalSessions": 8,
        "attendedSessions": 6,
        "attendanceRate": 75.0,
        "sessions": [
          // ... 세션 정보
        ]
      }
    ]
  }
]
```

#### 필드 설명

**MultiCourseCategoryResponse** (태그별 그룹)
| 필드 | 타입 | 설명 |
|-----|------|------|
| tags | string | 태그명 (예: "기본훈련", "행동교정") |
| courses | MultiCourseGroupResponse[] | 해당 태그의 코스 배열 |

**MultiCourseGroupResponse** (개별 코스)
| 필드 | 타입 | 설명 |
|-----|------|------|
| courseId | number | 코스 고유 ID |
| title | string | 코스 제목 |
| tags | string | 태그 |
| description | string | 코스 설명 |
| location | string | 장소 |
| type | string | 코스 타입 (항상 `"MULTI"`) |
| difficulty | string | 난이도 (`BEGINNER`/`INTERMEDIATE`/`ADVANCED`) |
| mainImage | string | 메인 이미지 URL |
| totalSessions | number | 전체 세션 수 |
| attendedSessions | number | 출석한 세션 수 |
| attendanceRate | number | 출석률 (%) |
| sessions | MultiSessionResponse[] | 세션 상세 정보 배열 |

**MultiSessionResponse** (개별 세션)
| 필드 | 타입 | 설명 |
|-----|------|------|
| sessionId | number | 세션 고유 ID |
| sessionNo | number | 회차 번호 (1, 2, 3...) |
| sessionDate | string | 세션 날짜 (YYYY-MM-DD) |
| startTime | string | 시작 시간 (HH:mm:ss) |
| endTime | string | 종료 시간 (HH:mm:ss) |
| locationDetail | string | 상세 위치 |
| attendanceStatus | string \| null | 출석 상태 (`"ATTENDED"`, `"ABSENT"`, `null`) |

> 💡 **attendanceStatus 값**:
> - `"ATTENDED"`: 출석함
> - `"ABSENT"`: 결석함
> - `null`: 아직 진행되지 않은 세션 (예정)

---

## 실제 응답 예시

### 📦 전체 응답 구조 (실제 데이터)

```json
{
  "dog": {
    "dogId": 1,
    "dogName": "멍멍이",
    "breed": "골든 리트리버",
    "age": 3,
    "gender": "MALE",
    "weight": 28.5,
    "profileImage": "https://mungtrainer-s3.s3.ap-northeast-2.amazonaws.com/dog-profiles/dog-1/profile.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=900&...",
    "neutered": true,
    "registeredDate": "2024-01-10T09:00:00"
  },
  "counselings": [
    {
      "counselingId": 101,
      "dogId": 1,
      "content": "산책 시 다른 개를 보면 짖는 문제가 있어 집중 훈련 필요",
      "trainerId": 5,
      "isCompleted": true,
      "createdAt": "2024-11-01T10:30:00",
      "updatedAt": "2024-11-05T14:20:00"
    }
  ],
  "stats": {
    "timesApplied": 5,
    "attendedCount": 12
  },
  "trainingApplications": [
    {
      "courseId": 201,
      "courseTitle": "기본 복종 훈련",
      "courseDescription": "앉아, 엎드려, 기다려 등 기본 명령어 훈련",
      "tags": "기본훈련",
      "type": "SINGLE",
      "sessionId": 301,
      "sessionDate": "2024-11-15",
      "sessionStartTime": "10:00:00",
      "sessionEndTime": "11:00:00"
    }
  ],
  "multiCourses": [
    {
      "tags": "기본훈련",
      "courses": [
        {
          "courseId": 101,
          "title": "퍼피 기초 훈련 과정",
          "tags": "기본훈련",
          "description": "강아지 시기에 배워야 할 기본 훈련",
          "location": "강남센터",
          "type": "MULTI",
          "difficulty": "BEGINNER",
          "mainImage": "https://s3.../course-101.jpg",
          "totalSessions": 10,
          "attendedSessions": 8,
          "attendanceRate": 80.0,
          "sessions": [
            {
              "sessionId": 1001,
              "sessionNo": 1,
              "sessionDate": "2024-11-01",
              "startTime": "10:00:00",
              "endTime": "11:00:00",
              "locationDetail": "강남센터 1층 훈련장",
              "attendanceStatus": "ATTENDED"
            }
          ]
        }
      ]
    }
  ]
}
```

---

## TypeScript 인터페이스

### 📘 완전한 타입 정의

```typescript
// types/dog-stats.ts

// ============ 최상위 응답 타입 ============
export interface DogStatsResponse {
  dog: DogResponse;
  counselings: CounselingResponse[];
  stats: Stats;
  trainingApplications: TrainingSessionDto[];
  multiCourses: MultiCourseCategoryResponse[];
}

// ============ 반려견 정보 ============
export type Gender = 'MALE' | 'FEMALE';

export interface DogResponse {
  dogId: number;
  dogName: string;
  breed: string;
  age: number;
  gender: Gender;
  weight: number;
  profileImage: string | null;
  neutered: boolean;
  registeredDate: string;
}

// ============ 상담 기록 ============
export interface CounselingResponse {
  counselingId: number;
  dogId: number;
  content: string;
  trainerId: number;
  isCompleted: boolean;
  createdAt: string;
  updatedAt: string;
}

// ============ 통계 요약 ============
export interface Stats {
  timesApplied: number;
  attendedCount: number;
}

// ============ 단회차 훈련 ============
export type CourseType = 'SINGLE' | 'MULTI';

export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  tags: string;
  type: CourseType;
  sessionId: number;
  sessionDate: string;  // YYYY-MM-DD
  sessionStartTime: string;  // HH:mm:ss
  sessionEndTime: string;    // HH:mm:ss
}

// ============ 다회차 훈련 ============
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type AttendanceStatus = 'ATTENDED' | 'ABSENT' | null;

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
  type: CourseType;
  difficulty: Difficulty;
  mainImage: string;
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: MultiSessionResponse[];
}

export interface MultiSessionResponse {
  sessionId: number;
  sessionNo: number;
  sessionDate: string;  // YYYY-MM-DD
  startTime: string;    // HH:mm:ss
  endTime: string;      // HH:mm:ss
  locationDetail: string;
  attendanceStatus: AttendanceStatus;
}
```

---

## 프론트엔드 구현 예시

### 🎯 React Query Hook

```typescript
// hooks/useDogStats.ts
import { useQuery } from '@tanstack/react-query';
import { DogStatsResponse } from '@/types/dog-stats';

export const useDogStats = (dogId: number) => {
  return useQuery<DogStatsResponse>({
    queryKey: ['dogStats', dogId],
    queryFn: async () => {
      const response = await fetch(`/api/trainer/user/dogs/${dogId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch dog stats');
      }

      return response.json();
    },
    enabled: !!dogId,
    staleTime: 5 * 60 * 1000, // 5분
  });
};
```

### 📱 메인 페이지 컴포넌트

```typescript
// app/trainer/dogs/[dogId]/stats/page.tsx
'use client';

import { useDogStats } from '@/hooks/useDogStats';
import { useParams } from 'next/navigation';
import DogProfileCard from '@/components/DogProfileCard';
import CounselingHistory from '@/components/CounselingHistory';
import StatsOverview from '@/components/StatsOverview';
import SingleTrainingList from '@/components/SingleTrainingList';
import MultiCourseCategories from '@/components/MultiCourseCategories';

export default function DogStatsPage() {
  const params = useParams();
  const dogId = Number(params.dogId);

  const { data, isLoading, error } = useDogStats(dogId);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center text-red-600">
          <p className="text-xl font-bold">⚠️ 오류 발생</p>
          <p className="mt-2">{error.message}</p>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-gray-600">데이터가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 1. 반려견 프로필 */}
      <DogProfileCard dog={data.dog} />

      {/* 2. 통계 요약 */}
      <StatsOverview stats={data.stats} />

      {/* 3. 상담 기록 */}
      <CounselingHistory counselings={data.counselings} />

      {/* 4. 단회차 훈련 목록 */}
      <SingleTrainingList trainings={data.trainingApplications} />

      {/* 5. 다회차 훈련 (태그별) */}
      <MultiCourseCategories categories={data.multiCourses} />
    </div>
  );
}
```

---

## UI 컴포넌트 설계

### 1️⃣ 반려견 프로필 카드

```typescript
// components/DogProfileCard.tsx
import { DogResponse } from '@/types/dog-stats';
import Image from 'next/image';

interface Props {
  dog: DogResponse;
}

export default function DogProfileCard({ dog }: Props) {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="flex items-center gap-6">
        {/* 프로필 이미지 */}
        {dog.profileImage ? (
          <Image
            src={dog.profileImage}
            alt={dog.dogName}
            width={120}
            height={120}
            className="rounded-full object-cover"
            unoptimized
          />
        ) : (
          <div className="w-30 h-30 bg-gradient-to-br from-blue-100 to-purple-100 rounded-full flex items-center justify-center">
            <span className="text-6xl">🐕</span>
          </div>
        )}

        {/* 기본 정보 */}
        <div className="flex-1">
          <h1 className="text-3xl font-bold mb-2">{dog.dogName}</h1>
          <div className="grid grid-cols-2 gap-4 text-gray-700">
            <div>
              <span className="text-sm text-gray-500">견종</span>
              <p className="font-semibold">{dog.breed}</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">나이</span>
              <p className="font-semibold">{dog.age}세</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">성별</span>
              <p className="font-semibold">{dog.gender === 'MALE' ? '수컷' : '암컷'}</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">체중</span>
              <p className="font-semibold">{dog.weight}kg</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">중성화</span>
              <p className="font-semibold">{dog.neutered ? '✅ 완료' : '❌ 미완료'}</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">등록일</span>
              <p className="font-semibold">
                {new Date(dog.registeredDate).toLocaleDateString('ko-KR')}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
```

### 2️⃣ 통계 요약 카드

```typescript
// components/StatsOverview.tsx
import { Stats } from '@/types/dog-stats';

interface Props {
  stats: Stats;
}

export default function StatsOverview({ stats }: Props) {
  const attendanceRate = stats.timesApplied > 0
    ? ((stats.attendedCount / stats.timesApplied) * 100).toFixed(1)
    : 0;

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {/* 총 신청 횟수 */}
      <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-6 shadow">
        <div className="text-4xl mb-2">📚</div>
        <div className="text-3xl font-bold text-blue-700">{stats.timesApplied}</div>
        <div className="text-sm text-blue-600 mt-1">총 신청 횟수</div>
      </div>

      {/* 총 출석 횟수 */}
      <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-6 shadow">
        <div className="text-4xl mb-2">✅</div>
        <div className="text-3xl font-bold text-green-700">{stats.attendedCount}</div>
        <div className="text-sm text-green-600 mt-1">총 출석 횟수</div>
      </div>

      {/* 출석률 */}
      <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg p-6 shadow">
        <div className="text-4xl mb-2">📊</div>
        <div className="text-3xl font-bold text-purple-700">{attendanceRate}%</div>
        <div className="text-sm text-purple-600 mt-1">출석률</div>
      </div>
    </div>
  );
}
```

### 3️⃣ 상담 기록

```typescript
// components/CounselingHistory.tsx
import { CounselingResponse } from '@/types/dog-stats';

interface Props {
  counselings: CounselingResponse[];
}

export default function CounselingHistory({ counselings }: Props) {
  if (counselings.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold mb-4">📝 상담 기록</h2>
        <p className="text-gray-500 text-center py-8">상담 기록이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-bold mb-4">📝 상담 기록</h2>
      <div className="space-y-4">
        {counselings.map((counseling) => (
          <div
            key={counseling.counselingId}
            className="border-l-4 border-blue-500 pl-4 py-3 bg-gray-50 rounded-r"
          >
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <span className={`px-2 py-1 rounded text-xs font-semibold ${
                  counseling.isCompleted
                    ? 'bg-green-100 text-green-800'
                    : 'bg-yellow-100 text-yellow-800'
                }`}>
                  {counseling.isCompleted ? '완료' : '진행중'}
                </span>
                <span className="text-sm text-gray-500">
                  {new Date(counseling.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
            </div>
            <p className="text-gray-800">{counseling.content}</p>
            {counseling.updatedAt !== counseling.createdAt && (
              <p className="text-xs text-gray-500 mt-2">
                수정됨: {new Date(counseling.updatedAt).toLocaleDateString('ko-KR')}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
```

### 4️⃣ 단회차 훈련 목록

```typescript
// components/SingleTrainingList.tsx
import { TrainingSessionDto } from '@/types/dog-stats';

interface Props {
  trainings: TrainingSessionDto[];
}

export default function SingleTrainingList({ trainings }: Props) {
  if (trainings.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold mb-4">🎯 단회차 훈련 이력</h2>
        <p className="text-gray-500 text-center py-8">단회차 훈련 이력이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-bold mb-4">🎯 단회차 훈련 이력</h2>
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-semibold">태그</th>
              <th className="px-4 py-3 text-left text-sm font-semibold">코스명</th>
              <th className="px-4 py-3 text-left text-sm font-semibold">날짜</th>
              <th className="px-4 py-3 text-left text-sm font-semibold">시간</th>
            </tr>
          </thead>
          <tbody>
            {trainings.map((training) => (
              <tr key={training.sessionId} className="border-b hover:bg-gray-50">
                <td className="px-4 py-3">
                  <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-semibold">
                    {training.tags}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div>
                    <p className="font-semibold">{training.courseTitle}</p>
                    <p className="text-sm text-gray-600">{training.courseDescription}</p>
                  </div>
                </td>
                <td className="px-4 py-3">
                  {new Date(training.sessionDate).toLocaleDateString('ko-KR')}
                </td>
                <td className="px-4 py-3 text-sm">
                  {training.sessionStartTime.slice(0, 5)} ~ {training.sessionEndTime.slice(0, 5)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
```

### 5️⃣ 다회차 훈련 (가장 복잡!) ⭐

```typescript
// components/MultiCourseCategories.tsx
import { MultiCourseCategoryResponse } from '@/types/dog-stats';
import MultiCourseCard from './MultiCourseCard';

interface Props {
  categories: MultiCourseCategoryResponse[];
}

export default function MultiCourseCategories({ categories }: Props) {
  if (categories.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-bold mb-4">📚 다회차 훈련 이력</h2>
        <p className="text-gray-500 text-center py-8">다회차 훈련 이력이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <h2 className="text-2xl font-bold">📚 다회차 훈련 이력</h2>
      
      {categories.map((category) => (
        <div key={category.tags} className="bg-white rounded-lg shadow-md p-6">
          {/* 태그별 제목 */}
          <div className="flex items-center gap-3 mb-6">
            <span className="bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-2 rounded-full text-lg font-bold">
              {category.tags}
            </span>
            <span className="text-gray-500">
              {category.courses.length}개 코스
            </span>
          </div>

          {/* 코스 목록 */}
          <div className="space-y-6">
            {category.courses.map((course) => (
              <MultiCourseCard key={course.courseId} course={course} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
```

```typescript
// components/MultiCourseCard.tsx
import { MultiCourseGroupResponse } from '@/types/dog-stats';
import { useState } from 'react';
import SessionTimeline from './SessionTimeline';

interface Props {
  course: MultiCourseGroupResponse;
}

export default function MultiCourseCard({ course }: Props) {
  const [isExpanded, setIsExpanded] = useState(false);

  const difficultyColor = {
    BEGINNER: 'bg-green-100 text-green-800',
    INTERMEDIATE: 'bg-yellow-100 text-yellow-800',
    ADVANCED: 'bg-red-100 text-red-800',
  };

  const difficultyLabel = {
    BEGINNER: '초급',
    INTERMEDIATE: '중급',
    ADVANCED: '고급',
  };

  return (
    <div className="border rounded-lg overflow-hidden">
      {/* 코스 헤더 */}
      <div
        className="bg-gradient-to-r from-gray-50 to-gray-100 p-4 cursor-pointer hover:from-gray-100 hover:to-gray-200 transition"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-lg font-bold">{course.title}</h3>
              <span className={`px-2 py-1 rounded text-xs font-semibold ${difficultyColor[course.difficulty]}`}>
                {difficultyLabel[course.difficulty]}
              </span>
            </div>
            <p className="text-sm text-gray-600 mb-2">{course.description}</p>
            <div className="flex items-center gap-4 text-sm text-gray-500">
              <span>📍 {course.location}</span>
              <span>📅 총 {course.totalSessions}회</span>
              <span>✅ 출석 {course.attendedSessions}회</span>
              <span className="font-semibold text-blue-600">
                출석률: {course.attendanceRate.toFixed(1)}%
              </span>
            </div>
          </div>
          
          {/* 펼치기/접기 버튼 */}
          <button className="text-2xl ml-4">
            {isExpanded ? '🔼' : '🔽'}
          </button>
        </div>

        {/* 출석률 프로그레스 바 */}
        <div className="mt-3 bg-gray-200 rounded-full h-2 overflow-hidden">
          <div
            className="bg-gradient-to-r from-blue-500 to-green-500 h-full transition-all duration-300"
            style={{ width: `${course.attendanceRate}%` }}
          />
        </div>
      </div>

      {/* 세션 상세 (펼쳤을 때만 표시) */}
      {isExpanded && (
        <div className="p-4 bg-white">
          <SessionTimeline sessions={course.sessions} />
        </div>
      )}
    </div>
  );
}
```

```typescript
// components/SessionTimeline.tsx
import { MultiSessionResponse } from '@/types/dog-stats';

interface Props {
  sessions: MultiSessionResponse[];
}

export default function SessionTimeline({ sessions }: Props) {
  const getStatusColor = (status: string | null) => {
    if (status === 'ATTENDED') return 'bg-green-500';
    if (status === 'ABSENT') return 'bg-red-500';
    return 'bg-gray-300';
  };

  const getStatusLabel = (status: string | null) => {
    if (status === 'ATTENDED') return '출석';
    if (status === 'ABSENT') return '결석';
    return '예정';
  };

  return (
    <div className="space-y-3">
      <h4 className="font-semibold text-gray-700 mb-4">세션 상세</h4>
      {sessions.map((session, index) => (
        <div key={session.sessionId} className="flex items-start gap-4">
          {/* 타임라인 점 */}
          <div className="flex flex-col items-center">
            <div className={`w-4 h-4 rounded-full ${getStatusColor(session.attendanceStatus)}`} />
            {index < sessions.length - 1 && (
              <div className="w-0.5 h-full bg-gray-300 mt-1" style={{ minHeight: '40px' }} />
            )}
          </div>

          {/* 세션 정보 */}
          <div className="flex-1 pb-4">
            <div className="flex items-center gap-2 mb-1">
              <span className="font-semibold">{session.sessionNo}회차</span>
              <span className={`px-2 py-0.5 rounded text-xs font-semibold ${
                session.attendanceStatus === 'ATTENDED'
                  ? 'bg-green-100 text-green-800'
                  : session.attendanceStatus === 'ABSENT'
                  ? 'bg-red-100 text-red-800'
                  : 'bg-gray-100 text-gray-800'
              }`}>
                {getStatusLabel(session.attendanceStatus)}
              </span>
            </div>
            <p className="text-sm text-gray-600">
              📅 {new Date(session.sessionDate).toLocaleDateString('ko-KR')}
            </p>
            <p className="text-sm text-gray-600">
              ⏰ {session.startTime.slice(0, 5)} ~ {session.endTime.slice(0, 5)}
            </p>
            <p className="text-sm text-gray-600">
              📍 {session.locationDetail}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}
```

---

## 주의사항 및 트러블슈팅

### ⚠️ 주요 주의사항

#### 1. **프로필 이미지 Presigned URL**
```typescript
// ❌ 잘못된 방법: URL을 15분 이상 캐싱
const { data } = useQuery({
  staleTime: 30 * 60 * 1000, // 30분 - URL 만료!
});

// ✅ 올바른 방법
const { data } = useQuery({
  staleTime: 5 * 60 * 1000,  // 5분
  refetchInterval: 10 * 60 * 1000, // 10분마다 갱신
});
```

#### 2. **multiCourses 데이터 구조 이해**
```typescript
// ❌ 잘못된 접근
data.multiCourses.map(course => ...)  // 틀림! courses는 한 단계 더 안에 있음

// ✅ 올바른 접근
data.multiCourses.map(category => 
  category.courses.map(course => ...)
)
```

#### 3. **null/undefined 체크**
```typescript
// attendanceStatus는 null일 수 있음
session.attendanceStatus === 'ATTENDED'  // ✅
session.attendanceStatus == 'ATTENDED'   // ❌ (null과 혼동 가능)

// 배열이 비어있을 수 있음
if (data.counselings.length === 0) {
  return <EmptyState />;
}
```

#### 4. **날짜/시간 포맷팅**
```typescript
// ISO 8601 문자열을 Date 객체로 변환
const date = new Date(session.sessionDate);

// 한국 로케일로 표시
date.toLocaleDateString('ko-KR')  // "2024년 11월 15일"

// 시간은 slice로 잘라서 표시
session.startTime.slice(0, 5)  // "10:00:00" → "10:00"
```

### 🐛 트러블슈팅

#### **문제 1: 이미지가 안 보임**
```
원인: Presigned URL 만료 (15분 경과)
해결: refetch() 호출 또는 자동 갱신 설정
```

#### **문제 2: multiCourses가 빈 배열로 표시됨**
```
원인: 다회차 훈련 신청이 없는 경우
해결: 빈 배열 체크 후 EmptyState 컴포넌트 표시
```

#### **문제 3: attendanceRate가 NaN으로 표시됨**
```typescript
// 원인: totalSessions가 0일 때 나누기 연산
const rate = attendedSessions / totalSessions; // NaN!

// 해결: 조건부 처리
const rate = totalSessions > 0 
  ? (attendedSessions / totalSessions) * 100 
  : 0;
```

#### **문제 4: 세션이 중복으로 표시됨**
```
원인: 백엔드에서 그룹핑 시 세션이 중복 추가됨
해결: 백엔드 로직 확인 또는 프론트에서 중복 제거
```

```typescript
// 프론트에서 중복 제거
const uniqueSessions = sessions.filter(
  (session, index, self) =>
    index === self.findIndex(s => s.sessionId === session.sessionId)
);
```

### 📊 데이터 시각화 추천

#### **1. 출석률 차트 (Chart.js / Recharts)**
```typescript
import { Doughnut } from 'react-chartjs-2';

const chartData = {
  labels: ['출석', '결석'],
  datasets: [{
    data: [stats.attendedCount, stats.timesApplied - stats.attendedCount],
    backgroundColor: ['#10b981', '#ef4444'],
  }],
};

<Doughnut data={chartData} />
```

#### **2. 태그별 훈련 분포**
```typescript
const tagCounts = multiCourses.reduce((acc, category) => {
  acc[category.tags] = category.courses.length;
  return acc;
}, {} as Record<string, number>);
```

### 🎨 UI/UX 개선 아이디어

1. **스켈레톤 로딩**: 데이터 로딩 중 스켈레톤 UI 표시
2. **애니메이션**: 출석률 프로그레스 바 애니메이션
3. **필터링**: 태그별, 기간별 필터 추가
4. **정렬**: 최신순, 출석률순 정렬
5. **검색**: 코스명으로 검색
6. **모달**: 세션 클릭 시 상세 정보 모달
7. **인쇄**: PDF 출력 기능

---

## 📞 API 테스트

### cURL 예시
```bash
curl -X GET "http://localhost:8080/api/trainer/user/dogs/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 응답 확인 체크리스트
- [ ] `dog` 객체에 `profileImage`가 Presigned URL로 반환되는가?
- [ ] `counselings` 배열이 존재하는가?
- [ ] `stats.timesApplied`와 `stats.attendedCount`가 숫자인가?
- [ ] `trainingApplications` 배열의 각 항목에 `sessionDate`가 있는가?
- [ ] `multiCourses`가 태그별로 그룹화되어 있는가?
- [ ] `sessions` 배열에 `attendanceStatus`가 포함되어 있는가?

---

## 📚 참고 자료

- **백엔드 서비스**: `TrainerUserService.getDogStats()`
- **DTO**: `DogStatsResponse.java`
- **MyBatis XML**: `TrainerUserDAO.xml` - `findMultiCourseDetail`

---

**마지막 업데이트**: 2025-01-23  
**API 버전**: v1.0  
**작성자**: Backend Team

---

## 💡 빠른 시작 가이드

### 1단계: 타입 정의 복사
`types/dog-stats.ts` 파일에 위의 TypeScript 인터페이스 복사

### 2단계: Hook 생성
`hooks/useDogStats.ts` 파일에 React Query Hook 복사

### 3단계: 컴포넌트 작성
각 섹션별로 컴포넌트 분리하여 작성

### 4단계: 페이지 조합
메인 페이지에서 모든 컴포넌트 조합

### 5단계: 스타일링
Tailwind CSS 또는 원하는 스타일링 라이브러리로 꾸미기

---

**이 문서만 있으면 프론트엔드 개발 완료! 🎉**

