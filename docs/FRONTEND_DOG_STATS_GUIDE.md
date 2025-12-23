# 🐕 반려견 통계 페이지 API - 프론트엔드 개발 가이드

> **실제 응답 데이터 기준 (2025-12-23 업데이트)**  
> dogId=6 실제 응답을 기반으로 작성된 완전한 프론트엔드 가이드

---

## 📋 목차
1. [API 개요](#api-개요)
2. [실제 응답 데이터 분석](#실제-응답-데이터-분석)
3. [TypeScript 인터페이스](#typescript-인터페이스)
4. [프론트엔드 구현 예시](#프론트엔드-구현-예시)
5. [UI 컴포넌트 완전 가이드](#ui-컴포넌트-완전-가이드)
6. [주의사항](#주의사항)

---

## API 개요

### 📌 기본 정보
- **Endpoint**: `GET /api/trainer/user/dogs/{dogId}`
- **설명**: 특정 반려견의 전체 정보 + 훈련 이력 + 상담 기록 + 통계를 조회
- **인증**: 필수 (JWT Token)

### 📝 Request
```typescript
const response = await fetch(`/api/trainer/user/dogs/${dogId}`, {
  headers: {
    'Authorization': `Bearer ${token}`,
  },
});
```

---

## 실제 응답 데이터 분석

### 🎯 실제 API 응답 (dogId=6)

```json
{
  "dog": {
    "dogId": 6,
    "name": "뿌뿌",
    "breed": "포메",
    "age": 0,
    "gender": "M",
    "isNeutered": true,
    "weight": null,
    "personality": null,
    "habits": null,
    "healthInfo": null,
    "humanSocialization": "MEDIUM",
    "animalSocialization": "MEDIUM",
    "profileImage": "https://mungschool.s3.ap-northeast-2.amazonaws.com/dog-profile/2/dog2-1766297465500.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251223T052706Z&...",
    "createdAt": "2025-12-21T15:11:05",
    "updatedAt": "2025-12-21T15:11:05"
  },
  "counselings": [],
  "stats": {
    "timesApplied": 3,
    "attendedCount": 1
  },
  "trainingApplications": [],
  "multiCourses": [
    {
      "tags": "기초,사회화,복종",
      "courses": [
        {
          "courseId": 1,
          "title": "강아지 기초 훈련 4주 코스",
          "tags": "기초,사회화,복종",
          "description": "4주 동안 진행되는 기초 훈련 과정입니다. 앉아, 기다려, 이리와 등 기본 명령어를 배웁니다.",
          "location": "서울시 강남구 테헤란로 123",
          "type": "MULTI",
          "difficulty": "BEGINNER",
          "mainImage": "course/1/main.jpg",
          "totalSessions": 3,
          "attendedSessions": 1,
          "attendanceRate": 33.333333333333336,
          "sessions": [
            {
              "sessionId": 1,
              "sessionNo": 1,
              "sessionDate": "2026-01-10",
              "startTime": "14:00",
              "endTime": "15:30",
              "locationDetail": "강남센터 1층 101호",
              "attendanceStatus": "ATTENDED"
            },
            {
              "sessionId": 2,
              "sessionNo": 2,
              "sessionDate": "2026-01-17",
              "startTime": "14:00",
              "endTime": "15:30",
              "locationDetail": "강남센터 1층 101호",
              "attendanceStatus": null
            },
            {
              "sessionId": 3,
              "sessionNo": 3,
              "sessionDate": "2026-01-24",
              "startTime": "14:00",
              "endTime": "15:30",
              "locationDetail": "강남센터 1층 101호",
              "attendanceStatus": null
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

### 📘 완전한 타입 정의 (실제 응답 기준)

```typescript
// types/dog-stats.ts

// ============ 최상위 응답 ============
export interface DogStatsResponse {
  dog: DogResponse;
  counselings: CounselingResponse[];
  stats: Stats;
  trainingApplications: TrainingSessionDto[];
  multiCourses: MultiCourseCategoryResponse[];
}

// ============ 반려견 정보 (완전판) ============
export type Gender = 'M' | 'F';
export type SocializationLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface DogResponse {
  dogId: number;
  name: string;                           // ⚠️ dogName이 아님!
  breed: string;
  age: number;
  gender: Gender;                         // ⚠️ "M" 또는 "F"
  isNeutered: boolean;                    // ⚠️ neutered가 아님!
  weight: number | null;                  // nullable
  personality: string | null;             // nullable
  habits: string | null;                  // nullable
  healthInfo: string | null;              // nullable
  humanSocialization: SocializationLevel;
  animalSocialization: SocializationLevel;
  profileImage: string | null;            // S3 Presigned URL
  createdAt: string;                      // ISO 8601
  updatedAt: string;                      // ISO 8601
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
  timesApplied: number;    // 전체 신청 횟수 (단회차 + 다회차)
  attendedCount: number;   // 전체 출석 횟수 (단회차 + 다회차)
}

// ============ 단회차 훈련 ============
export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  courseDescription: string;
  tags: string;
  type: 'SINGLE';
  sessionId: number;
  sessionDate: string;         // YYYY-MM-DD
  sessionStartTime: string;    // HH:mm:ss
  sessionEndTime: string;      // HH:mm:ss
}

// ============ 다회차 훈련 ============
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type AttendanceStatus = 'ATTENDED' | 'ABSENT' | null;

export interface MultiCourseCategoryResponse {
  tags: string;                          // 쉼표로 구분된 태그들
  courses: MultiCourseGroupResponse[];
}

export interface MultiCourseGroupResponse {
  courseId: number;
  title: string;
  tags: string;                          // 쉼표로 구분
  description: string;
  location: string;
  type: 'MULTI';
  difficulty: Difficulty;
  mainImage: string;
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;                // 소수점 포함 (33.333333...)
  sessions: MultiSessionResponse[];
}

export interface MultiSessionResponse {
  sessionId: number;
  sessionNo: number;
  sessionDate: string;                   // YYYY-MM-DD (⚠️ time 없음!)
  startTime: string;                     // HH:mm (⚠️ :ss 없음!)
  endTime: string;                       // HH:mm (⚠️ :ss 없음!)
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
    staleTime: 5 * 60 * 1000,     // 5분
    refetchInterval: 10 * 60 * 1000, // 10분마다 자동 갱신 (Presigned URL 만료 대비)
  });
};
```

---

## UI 컴포넌트 완전 가이드

### 📱 메인 페이지

```typescript
// app/trainer/dogs/[dogId]/stats/page.tsx
'use client';

import { useDogStats } from '@/hooks/useDogStats';
import { useParams } from 'next/navigation';
import DogProfileSection from '@/components/DogProfileSection';
import StatsOverview from '@/components/StatsOverview';
import CounselingHistory from '@/components/CounselingHistory';
import MultiCourseList from '@/components/MultiCourseList';

export default function DogStatsPage() {
  const params = useParams();
  const dogId = Number(params.dogId);
  const { data, isLoading, error } = useDogStats(dogId);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center text-red-600 p-8">
        <p className="text-xl font-bold">⚠️ 오류 발생</p>
        <p>{error.message}</p>
      </div>
    );
  }

  if (!data) return <div>데이터가 없습니다.</div>;

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 1. 반려견 프로필 */}
      <DogProfileSection dog={data.dog} />

      {/* 2. 통계 요약 */}
      <StatsOverview stats={data.stats} />

      {/* 3. 상담 기록 */}
      {data.counselings.length > 0 && (
        <CounselingHistory counselings={data.counselings} />
      )}

      {/* 4. 다회차 훈련 */}
      {data.multiCourses.length > 0 && (
        <MultiCourseList multiCourses={data.multiCourses} />
      )}

      {/* 빈 상태 처리 */}
      {data.trainingApplications.length === 0 && data.multiCourses.length === 0 && (
        <div className="text-center p-12 bg-gray-50 rounded-lg">
          <p className="text-gray-600">아직 신청한 훈련이 없습니다.</p>
        </div>
      )}
    </div>
  );
}
```

### 1️⃣ 반려견 프로필 섹션 (완전판)

```typescript
// components/DogProfileSection.tsx
import { DogResponse } from '@/types/dog-stats';
import Image from 'next/image';

interface Props {
  dog: DogResponse;
}

export default function DogProfileSection({ dog }: Props) {
  // 사회화 수준 한글 변환
  const socializationLabel = {
    LOW: '낮음',
    MEDIUM: '보통',
    HIGH: '높음',
  };

  // 성별 표시
  const genderLabel = dog.gender === 'M' ? '수컷 ♂' : '암컷 ♀';

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <div className="flex items-start gap-6">
        {/* 프로필 이미지 */}
        {dog.profileImage ? (
          <Image
            src={dog.profileImage}
            alt={dog.name}
            width={150}
            height={150}
            className="rounded-full object-cover"
            unoptimized
          />
        ) : (
          <div className="w-[150px] h-[150px] bg-gradient-to-br from-blue-100 to-purple-100 rounded-full flex items-center justify-center">
            <span className="text-6xl">🐕</span>
          </div>
        )}

        {/* 기본 정보 */}
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-4">
            <h1 className="text-3xl font-bold">{dog.name}</h1>
            <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-semibold">
              {dog.breed}
            </span>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {/* 나이 */}
            <InfoItem label="나이" value={`${dog.age}세`} />

            {/* 성별 */}
            <InfoItem label="성별" value={genderLabel} />

            {/* 중성화 */}
            <InfoItem 
              label="중성화" 
              value={dog.isNeutered ? '✅ 완료' : '❌ 미완료'} 
            />

            {/* 체중 */}
            <InfoItem 
              label="체중" 
              value={dog.weight ? `${dog.weight}kg` : '미입력'} 
            />

            {/* 사람 사회화 */}
            <InfoItem 
              label="사람 사회화" 
              value={socializationLabel[dog.humanSocialization]} 
            />

            {/* 동물 사회화 */}
            <InfoItem 
              label="동물 사회화" 
              value={socializationLabel[dog.animalSocialization]} 
            />
          </div>

          {/* 선택 정보 (있을 경우만 표시) */}
          {(dog.personality || dog.habits || dog.healthInfo) && (
            <div className="mt-4 space-y-2">
              {dog.personality && (
                <DetailInfo label="성격" value={dog.personality} />
              )}
              {dog.habits && (
                <DetailInfo label="습관" value={dog.habits} />
              )}
              {dog.healthInfo && (
                <DetailInfo label="건강 정보" value={dog.healthInfo} />
              )}
            </div>
          )}

          {/* 등록일 */}
          <p className="text-sm text-gray-500 mt-4">
            등록일: {new Date(dog.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </div>
      </div>
    </div>
  );
}

// 하위 컴포넌트
function InfoItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span className="text-sm text-gray-500">{label}</span>
      <p className="font-semibold text-gray-900">{value}</p>
    </div>
  );
}

function DetailInfo({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-gray-50 p-3 rounded">
      <span className="text-sm font-semibold text-gray-700">{label}: </span>
      <span className="text-gray-800">{value}</span>
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
  // 출석률 계산
  const attendanceRate = stats.timesApplied > 0
    ? (stats.attendedCount / stats.timesApplied) * 100
    : 0;

  // 출석률에 따른 색상
  const getRateColor = (rate: number) => {
    if (rate >= 80) return 'from-green-50 to-green-100 text-green-700';
    if (rate >= 50) return 'from-yellow-50 to-yellow-100 text-yellow-700';
    return 'from-red-50 to-red-100 text-red-700';
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {/* 총 신청 */}
      <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-6 shadow">
        <div className="text-4xl mb-2">📚</div>
        <div className="text-3xl font-bold text-blue-700">{stats.timesApplied}</div>
        <div className="text-sm text-blue-600 mt-1">총 신청 횟수</div>
        <div className="text-xs text-blue-500 mt-2">단회차 + 다회차 전체</div>
      </div>

      {/* 총 출석 */}
      <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-lg p-6 shadow">
        <div className="text-4xl mb-2">✅</div>
        <div className="text-3xl font-bold text-green-700">{stats.attendedCount}</div>
        <div className="text-sm text-green-600 mt-1">총 출석 횟수</div>
        <div className="text-xs text-green-500 mt-2">출석 완료한 세션</div>
      </div>

      {/* 출석률 */}
      <div className={`bg-gradient-to-br rounded-lg p-6 shadow ${getRateColor(attendanceRate)}`}>
        <div className="text-4xl mb-2">📊</div>
        <div className="text-3xl font-bold">{attendanceRate.toFixed(1)}%</div>
        <div className="text-sm mt-1">전체 출석률</div>
        <div className="text-xs mt-2">
          {stats.attendedCount} / {stats.timesApplied}
        </div>
      </div>
    </div>
  );
}
```

### 3️⃣ 다회차 훈련 목록

```typescript
// components/MultiCourseList.tsx
import { MultiCourseCategoryResponse } from '@/types/dog-stats';
import MultiCourseCard from './MultiCourseCard';

interface Props {
  multiCourses: MultiCourseCategoryResponse[];
}

export default function MultiCourseList({ multiCourses }: Props) {
  return (
    <div className="space-y-8">
      <h2 className="text-2xl font-bold">📚 다회차 훈련 이력</h2>
      
      {multiCourses.map((category, idx) => (
        <div key={idx} className="bg-white rounded-lg shadow-md p-6">
          {/* 태그 헤더 */}
          <div className="flex items-center gap-3 mb-6">
            <span className="bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-2 rounded-full font-bold">
              {category.tags}
            </span>
            <span className="text-gray-600">
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

### 4️⃣ 개별 코스 카드 (세션 포함)

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

  const difficultyConfig = {
    BEGINNER: { label: '초급', color: 'bg-green-100 text-green-800' },
    INTERMEDIATE: { label: '중급', color: 'bg-yellow-100 text-yellow-800' },
    ADVANCED: { label: '고급', color: 'bg-red-100 text-red-800' },
  };

  const difficulty = difficultyConfig[course.difficulty];

  return (
    <div className="border rounded-lg overflow-hidden">
      {/* 코스 헤더 (클릭 가능) */}
      <div
        className="bg-gradient-to-r from-gray-50 to-gray-100 p-4 cursor-pointer hover:from-gray-100 hover:to-gray-200 transition"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-lg font-bold">{course.title}</h3>
              <span className={`px-2 py-1 rounded text-xs font-semibold ${difficulty.color}`}>
                {difficulty.label}
              </span>
            </div>
            <p className="text-sm text-gray-600 mb-3">{course.description}</p>
            
            <div className="flex flex-wrap items-center gap-4 text-sm text-gray-700">
              <span>📍 {course.location}</span>
              <span>📅 총 {course.totalSessions}회</span>
              <span>✅ 출석 {course.attendedSessions}회</span>
              <span className="font-semibold text-blue-600">
                출석률: {course.attendanceRate.toFixed(1)}%
              </span>
            </div>
          </div>
          
          <button className="text-2xl ml-4 transition-transform" style={{
            transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)'
          }}>
            🔽
          </button>
        </div>

        {/* 출석률 프로그레스 바 */}
        <div className="mt-3 bg-gray-200 rounded-full h-2 overflow-hidden">
          <div
            className="bg-gradient-to-r from-blue-500 to-green-500 h-full transition-all duration-300"
            style={{ width: `${Math.min(course.attendanceRate, 100)}%` }}
          />
        </div>
      </div>

      {/* 세션 상세 (펼쳤을 때만) */}
      {isExpanded && (
        <div className="p-4 bg-white border-t">
          <SessionTimeline sessions={course.sessions} />
        </div>
      )}
    </div>
  );
}
```

### 5️⃣ 세션 타임라인

```typescript
// components/SessionTimeline.tsx
import { MultiSessionResponse } from '@/types/dog-stats';

interface Props {
  sessions: MultiSessionResponse[];
}

export default function SessionTimeline({ sessions }: Props) {
  const getStatusInfo = (status: string | null) => {
    if (status === 'ATTENDED') {
      return { label: '출석', color: 'bg-green-500', textColor: 'text-green-800', bgColor: 'bg-green-100' };
    }
    if (status === 'ABSENT') {
      return { label: '결석', color: 'bg-red-500', textColor: 'text-red-800', bgColor: 'bg-red-100' };
    }
    return { label: '예정', color: 'bg-gray-300', textColor: 'text-gray-800', bgColor: 'bg-gray-100' };
  };

  return (
    <div className="space-y-3">
      <h4 className="font-semibold text-gray-700 mb-4">세션 상세</h4>
      {sessions.map((session, index) => {
        const statusInfo = getStatusInfo(session.attendanceStatus);
        
        return (
          <div key={session.sessionId} className="flex items-start gap-4">
            {/* 타임라인 점 */}
            <div className="flex flex-col items-center pt-1">
              <div className={`w-4 h-4 rounded-full ${statusInfo.color}`} />
              {index < sessions.length - 1 && (
                <div className="w-0.5 bg-gray-300 mt-1" style={{ height: '50px' }} />
              )}
            </div>

            {/* 세션 정보 */}
            <div className="flex-1 pb-4">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-semibold">{session.sessionNo}회차</span>
                <span className={`px-2 py-0.5 rounded text-xs font-semibold ${statusInfo.bgColor} ${statusInfo.textColor}`}>
                  {statusInfo.label}
                </span>
              </div>
              <p className="text-sm text-gray-600">
                📅 {new Date(session.sessionDate).toLocaleDateString('ko-KR', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  weekday: 'short'
                })}
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

## 주의사항

### ⚠️ 필드명 주의!

백엔드 응답과 일반적인 네이밍이 다른 필드들:

| 예상 | 실제 | 비고 |
|-----|------|------|
| `dogName` | `name` | ⚠️ |
| `neutered` | `isNeutered` | ⚠️ |
| `gender: "MALE"` | `gender: "M"` | ⚠️ |
| `startTime: "10:00:00"` | `startTime: "14:00"` | ⚠️ 초 없음 |

### 🔑 Null 처리 필수

다음 필드들은 `null`일 수 있습니다:

```typescript
if (dog.weight) {
  // weight가 있을 때만
}

if (dog.personality) {
  // 성격 정보가 있을 때만
}
```

### 📊 출석률 소수점 처리

```typescript
// ❌ 잘못된 방법
attendanceRate.toFixed(0)  // "33" - 너무 단순

// ✅ 올바른 방법
attendanceRate.toFixed(1)  // "33.3" - 적절
```

### 🖼️ 프로필 이미지 처리

```typescript
// Next.js Image 설정
<Image
  src={dog.profileImage || '/default-dog.png'}
  alt={dog.name}
  width={150}
  height={150}
  unoptimized  // S3 Presigned URL은 unoptimized 필요
/>

// 또는 next.config.js 설정
module.exports = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'mungschool.s3.ap-northeast-2.amazonaws.com',
      },
    ],
  },
};
```

### 📅 날짜/시간 포맷팅

```typescript
// 날짜 포맷팅
new Date(session.sessionDate).toLocaleDateString('ko-KR')
// "2026. 1. 10."

// 시간은 이미 "HH:mm" 형식
session.startTime  // "14:00" (⚠️ :ss 없음!)
```

---

## 📚 체크리스트

### 개발 전
- [ ] TypeScript 인터페이스 복사
- [ ] 필드명 차이 숙지 (`name`, `isNeutered`, `gender: "M"`)
- [ ] null 가능 필드 확인

### 개발 중
- [ ] React Query Hook 구현
- [ ] 5개 컴포넌트 작성
- [ ] null 체크 처리
- [ ] 빈 배열 처리 (counselings, trainingApplications)

### 테스트
- [ ] 프로필 이미지 표시 확인
- [ ] 출석률 계산 정확성 확인
- [ ] 세션 타임라인 표시 확인
- [ ] 빈 데이터 UI 확인

---

**업데이트**: 2025-12-23  
**기준 데이터**: dogId=6 실제 응답  
**문서 버전**: v2.0

