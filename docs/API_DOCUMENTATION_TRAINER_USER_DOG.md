# 훈련사 회원 및 반려견 관리 API 문서

## 🔄 변경 이력
- **2024-12-23**: 
  - ✅ `GET /api/trainer/users` 경로에서 `{trainerId}` path variable 제거
  - ✅ 사용자 프로필 이미지 필드 추가 및 S3 Presigned URL 자동 변환 적용
  - ✅ 반려견 프로필 이미지 S3 Presigned URL 처리 확인
  - ✅ 프론트엔드 이미지 출력 가이드 추가
  - ✅ 반려견 통계 API 상세 문서 작성 ([API_DOG_STATS_DETAIL.md](./API_DOG_STATS_DETAIL.md))
  - ✅ 실제 백엔드 응답 구조에 맞게 문서 전면 수정

## 📋 목차
1. [훈련사가 관리하는 회원 목록 조회](#1-훈련사가-관리하는-회원-목록-조회)
2. [회원의 반려견 목록 조회](#2-회원의-반려견-목록-조회)
3. [반려견 통계 정보 조회](#3-반려견-통계-정보-조회)

---

## 1. 훈련사가 관리하는 회원 목록 조회

### 📌 기본 정보
- **Endpoint**: `GET /api/trainer/users`
- **설명**: 로그인한 훈련사가 관리하는 모든 회원 목록을 조회합니다.
- **인증**: 필수 (JWT Token)

### 📝 Request

#### Path Parameters
없음 (JWT 토큰에서 trainerId를 자동으로 추출합니다)

#### Headers
```http
Authorization: Bearer {JWT_TOKEN}
```

#### Request Example (Next.js)
```typescript
// /api/trainer/users.ts 또는 컴포넌트 내부

const getTrainerUsers = async () => {
  try {
    const response = await fetch('/api/trainer/users', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error('회원 목록 조회 실패');
    }

    const data: TrainerUserListResponse[] = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching trainer users:', error);
    throw error;
  }
};
```

### 📤 Response

#### Success Response (200 OK)
```json
[
  {
    "userId": 1,
    "name": "김철수",
    "phone": "010-1234-5678",
    "email": "chulsoo@example.com",
    "profileImage": "https://mungtrainer-s3.s3.ap-northeast-2.amazonaws.com/user-profiles/user-1/profile.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=900&..."
  },
  {
    "userId": 2,
    "name": "이영희",
    "phone": "010-9876-5432",
    "email": "younghee@example.com",
    "profileImage": null
  }
]
```

#### Response Fields
| 필드 | 타입 | 설명 |
|-----|------|------|
| userId | number | 회원 고유 ID |
| name | string | 회원 이름 |
| phone | string | 전화번호 |
| email | string | 회원 이메일 |
| profileImage | string \| null | 프로필 이미지 S3 Presigned URL (유효기간 15분) |

> 📝 **중요사항**: 
> - `profileImage`는 **S3 Presigned URL**로 자동 변환되어 반환됩니다.
> - DB에는 S3 키(`user-profiles/123/profile.jpg`)가 저장되어 있지만, API 응답에서는 완전한 URL로 제공됩니다.
> - **Presigned URL 유효기간**: 15분 (900초)
> - 프로필 이미지가 없는 경우 `null`이 반환됩니다.
> - URL은 바로 `<img>` 태그의 `src`로 사용 가능합니다.

#### Error Response (401 Unauthorized)
```json
{
  "error": "Unauthorized",
  "message": "인증이 필요합니다."
}
```

#### Error Response (403 Forbidden)
```json
{
  "error": "Forbidden",
  "message": "훈련사 권한이 필요합니다."
}
```

### 🎨 Next.js 사용 예시

#### TypeScript Interface
```typescript
// types/trainer.ts
export interface TrainerUserListResponse {
  userId: number;
  name: string;
  phone: string;
  email: string;
  profileImage: string | null;  // S3 Presigned URL
}
```

#### React Component (App Router)
```typescript
// app/trainer/users/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { TrainerUserListResponse } from '@/types/trainer';
import Image from 'next/image';

export default function TrainerUsersPage() {
  const [users, setUsers] = useState<TrainerUserListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/trainer/users', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });

        if (!response.ok) throw new Error('Failed to fetch users');

        const data: TrainerUserListResponse[] = await response.json();
        setUsers(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '알 수 없는 오류');
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>오류: {error}</div>;

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">관리 중인 회원 목록</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {users.map((user) => (
          <div key={user.userId} className="border rounded-lg p-4 hover:shadow-lg transition">
            {/* 프로필 이미지 출력 */}
            <div className="flex items-center gap-4 mb-4">
              {user.profileImage ? (
                <Image
                  src={user.profileImage}
                  alt={user.name}
                  width={64}
                  height={64}
                  className="rounded-full object-cover"
                  unoptimized  // S3 Presigned URL은 외부 URL이므로 unoptimized 필요
                />
              ) : (
                <div className="w-16 h-16 bg-gray-300 rounded-full flex items-center justify-center">
                  <span className="text-gray-600 text-xl">👤</span>
                </div>
              )}
              <div>
                <h3 className="font-semibold text-lg">{user.name}</h3>
                <p className="text-sm text-gray-600">{user.phone}</p>
              </div>
            </div>
            <p className="text-sm">이메일: {user.email}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
```

> ⚠️ **Next.js Image 컴포넌트 사용 시 주의사항**:
> - S3 Presigned URL은 외부 URL이므로 `unoptimized` prop이 필요합니다.
> - 또는 `next.config.js`에 도메인을 추가해야 합니다:
> ```javascript
> // next.config.js
> module.exports = {
>   images: {
>     remotePatterns: [
>       {
>         protocol: 'https',
>         hostname: 'mungtrainer-s3.s3.ap-northeast-2.amazonaws.com',
>       },
>     ],
>   },
> };
> ```
```

#### React Query 사용 예시
```typescript
// hooks/useTrainerUsers.ts
import { useQuery } from '@tanstack/react-query';
import { TrainerUserListResponse } from '@/types/trainer';

export const useTrainerUsers = () => {
  return useQuery<TrainerUserListResponse[]>({
    queryKey: ['trainerUsers'],
    queryFn: async () => {
      const response = await fetch('/api/trainer/users', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch trainer users');
      }

      return response.json();
    },
    staleTime: 5 * 60 * 1000, // 5분간 캐시 유지 (Presigned URL 유효기간 고려)
  });
};

// 컴포넌트에서 사용
const { data: users, isLoading, error, refetch } = useTrainerUsers();
```

> 💡 **Tip**: Presigned URL은 15분간 유효하므로, `staleTime`을 5분 정도로 설정하고 주기적으로 `refetch`하는 것을 권장합니다.
```

---

## 2. 회원의 반려견 목록 조회

### 📌 기본 정보
- **Endpoint**: `GET /api/trainer/dogs/{userId}`
- **설명**: 특정 회원이 보유한 모든 반려견 목록을 조회합니다.
- **인증**: 필수 (JWT Token)

### 📝 Request

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 조회할 회원의 고유 ID |

#### Headers
```http
Authorization: Bearer {JWT_TOKEN}
```

#### Request Example (Next.js)
```typescript
const getUserDogs = async (userId: number) => {
  try {
    const response = await fetch(`/api/trainer/dogs/${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error('반려견 목록 조회 실패');
    }

    const data: DogResponse[] = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching user dogs:', error);
    throw error;
  }
};
```

### 📤 Response

#### Success Response (200 OK)
```json
[
  {
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
  {
    "dogId": 2,
    "dogName": "뽀삐",
    "breed": "푸들",
    "age": 2,
    "gender": "FEMALE",
    "weight": 5.2,
    "profileImage": null,
    "neutered": false,
    "registeredDate": "2024-03-15T11:30:00"
  }
]
```

#### Response Fields
| 필드 | 타입 | 설명 |
|-----|------|------|
| dogId | number | 반려견 고유 ID |
| dogName | string | 반려견 이름 |
| breed | string | 견종 |
| age | number | 나이 (년) |
| gender | string | 성별 (MALE/FEMALE) |
| weight | number | 체중 (kg) |
| profileImage | string \| null | 프로필 이미지 S3 Presigned URL (유효기간 15분) |
| neutered | boolean | 중성화 여부 |
| registeredDate | string (ISO 8601) | 등록일시 |

> 📝 **중요사항**: 
> - `profileImage`는 **S3 Presigned URL**로 자동 변환되어 반환됩니다.
> - DB에는 S3 키가 저장되어 있지만, API 응답에서는 완전한 URL로 제공됩니다.
> - **Presigned URL 유효기간**: 15분 (900초)
> - 프로필 이미지가 없는 경우 `null`이 반환됩니다.

#### Error Response (404 Not Found)
```json
{
  "error": "Not Found",
  "message": "해당 회원을 찾을 수 없습니다."
}
```

### 🎨 Next.js 사용 예시

#### TypeScript Interface
```typescript
// types/dog.ts
export type Gender = 'MALE' | 'FEMALE';

export interface DogResponse {
  dogId: number;
  dogName: string;
  breed: string;
  age: number;
  gender: Gender;
  weight: number;
  profileImage: string | null;  // S3 Presigned URL
  neutered: boolean;
  registeredDate: string;
}
```

#### React Component
```typescript
// components/DogList.tsx
'use client';

import { useEffect, useState } from 'react';
import { DogResponse } from '@/types/dog';
import Image from 'next/image';

interface DogListProps {
  userId: number;
}

export default function DogList({ userId }: DogListProps) {
  const [dogs, setDogs] = useState<DogResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDogs = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/trainer/dogs/${userId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
          },
        });

        if (!response.ok) throw new Error('Failed to fetch dogs');

        const data: DogResponse[] = await response.json();
        setDogs(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchDogs();
  }, [userId]);

  if (loading) return <div>로딩 중...</div>;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {dogs.map((dog) => (
        <div key={dog.dogId} className="border rounded-lg p-4 shadow hover:shadow-lg transition">
          {/* 반려견 프로필 이미지 출력 */}
          {dog.profileImage ? (
            <Image
              src={dog.profileImage}
              alt={dog.dogName}
              width={200}
              height={200}
              className="rounded-lg object-cover w-full h-48 mb-4"
              unoptimized  // S3 Presigned URL은 외부 URL이므로 필요
            />
          ) : (
            <div className="w-full h-48 bg-gray-200 rounded-lg flex items-center justify-center mb-4">
              <span className="text-4xl">🐕</span>
            </div>
          )}
          
          <h3 className="text-xl font-bold mt-2">{dog.dogName}</h3>
          <p className="text-gray-600">견종: {dog.breed}</p>
          <p className="text-gray-600">나이: {dog.age}세</p>
          <p className="text-gray-600">성별: {dog.gender === 'MALE' ? '수컷' : '암컷'}</p>
          <p className="text-gray-600">체중: {dog.weight}kg</p>
          <p className="text-gray-600">중성화: {dog.neutered ? '✅' : '❌'}</p>
        </div>
      ))}
    </div>
  );
}
```

> ⚠️ **프로필 이미지 출력 시 주의사항**:
> 1. **null 체크 필수**: `profileImage`가 `null`일 수 있으므로 조건부 렌더링 필요
> 2. **Next.js Image 설정**: 
>    - `unoptimized` prop 사용 또는
>    - `next.config.js`에 S3 도메인 추가
> 3. **Presigned URL 만료**: 15분 후 이미지가 로드되지 않을 수 있으므로 주기적으로 데이터 갱신 권장
```

#### Server Component (App Router)
```typescript
// app/trainer/users/[userId]/dogs/page.tsx
import { DogResponse } from '@/types/dog';
import { cookies } from 'next/headers';

async function getDogs(userId: string): Promise<DogResponse[]> {
  const cookieStore = cookies();
  const token = cookieStore.get('accessToken')?.value;

  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/api/trainer/dogs/${userId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      cache: 'no-store', // or 'force-cache' depending on your needs
    }
  );

  if (!response.ok) {
    throw new Error('Failed to fetch dogs');
  }

  return response.json();
}

export default async function UserDogsPage({
  params,
}: {
  params: { userId: string };
}) {
  const dogs = await getDogs(params.userId);

  return (
    <div>
      <h1>반려견 목록</h1>
      {dogs.map((dog) => (
        <div key={dog.dogId}>
          <h2>{dog.dogName}</h2>
          <p>{dog.breed}</p>
        </div>
      ))}
    </div>
  );
}
```

---

## 3. 반려견 통계 정보 조회

> 🚨 **중요**: 이 API는 복잡한 중첩 구조를 가지고 있습니다.  
> 📖 **상세 가이드**: [반려견 통계 페이지 API 상세 문서](./API_DOG_STATS_DETAIL.md)를 참고하세요.

### 📌 기본 정보
- **Endpoint**: `GET /api/trainer/user/dogs/{dogId}`
- **설명**: 특정 반려견의 **전체 훈련 이력, 상담 기록, 통계 정보**를 한 번에 조회합니다.
- **인증**: 필수 (JWT Token)
- **용도**: 반려견 상세 페이지, 훈련 이력 대시보드
- **복잡도**: ⭐⭐⭐⭐⭐ (가장 복잡한 API)

### 📝 Request

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| dogId | Long | ✅ | 조회할 반려견의 고유 ID |

#### Headers
```http
Authorization: Bearer {JWT_TOKEN}
```

#### Request Example (Next.js)
```typescript
const getDogStats = async (dogId: number) => {
  try {
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

    const data: DogStatsResponse = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching dog stats:', error);
    throw error;
  }
};
```

### 📤 Response

#### 📊 응답 구조 개요

이 API는 **5개의 주요 섹션**으로 구성된 복잡한 응답을 반환합니다:

```typescript
{
  dog: DogResponse,                     // 1️⃣ 반려견 기본 정보
  counselings: CounselingResponse[],    // 2️⃣ 상담 기록
  stats: Stats,                         // 3️⃣ 통계 요약
  trainingApplications: TrainingSessionDto[],  // 4️⃣ 단회차 훈련
  multiCourses: MultiCourseCategoryResponse[]  // 5️⃣ 다회차 훈련 (3단계 중첩!)
}
```

#### Success Response (200 OK)

```json
{
  "dog": {
    "dogId": 1,
    "dogName": "멍멍이",
    "breed": "골든 리트리버",
    "age": 3,
    "gender": "MALE",
    "weight": 28.5,
    "profileImage": "https://mungtrainer-s3.s3.ap-northeast-2.amazonaws.com/...",
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
      "courseDescription": "앉아, 엎드려, 기다려",
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
            },
            {
              "sessionId": 1002,
              "sessionNo": 2,
              "sessionDate": "2024-11-08",
              "startTime": "10:00:00",
              "endTime": "11:00:00",
              "locationDetail": "강남센터 1층 훈련장",
              "attendanceStatus": "ABSENT"
            }
          ]
        }
      ]
    }
  ]
}
```

#### Response Fields 요약

**1️⃣ dog** (DogResponse)
| 필드 | 타입 | 설명 |
|-----|------|------|
| dogId | number | 반려견 고유 ID |
| dogName | string | 반려견 이름 |
| breed | string | 견종 |
| profileImage | string \| null | S3 Presigned URL (15분 유효) |

**2️⃣ counselings** (CounselingResponse[])
| 필드 | 타입 | 설명 |
|-----|------|------|
| counselingId | number | 상담 ID |
| content | string | 상담 내용 |
| isCompleted | boolean | 완료 여부 |
| createdAt | string | 생성일시 |

**3️⃣ stats** (Stats)
| 필드 | 타입 | 설명 |
|-----|------|------|
| timesApplied | number | 총 신청 횟수 |
| attendedCount | number | 총 출석 횟수 |

**4️⃣ trainingApplications** (TrainingSessionDto[])
| 필드 | 타입 | 설명 |
|-----|------|------|
| courseId | number | 코스 ID |
| courseTitle | string | 코스 제목 |
| tags | string | 태그 |
| sessionDate | string | 세션 날짜 (YYYY-MM-DD) |

**5️⃣ multiCourses** (MultiCourseCategoryResponse[]) ⭐ **복잡!**
```
배열 구조:
└─ { tags, courses[] }
    └─ { courseId, title, totalSessions, sessions[] }
        └─ { sessionId, sessionNo, attendanceStatus }
```

> 📘 **상세 필드 설명 및 TypeScript 인터페이스**는 [상세 문서](./API_DOG_STATS_DETAIL.md)를 참고하세요.

#### Error Response (403 Forbidden)
```json
{
  "error": "Forbidden",
  "message": "해당 반려견에 대한 접근 권한이 없습니다."
}
```

#### Error Response (404 Not Found)
```json
{
  "error": "Not Found",
  "message": "반려견을 찾을 수 없습니다."
}
```

### 🎨 Next.js 사용 예시

#### TypeScript Interface (간략 버전)

> 📘 **완전한 타입 정의**는 [상세 문서](./API_DOG_STATS_DETAIL.md#typescript-인터페이스)를 참고하세요.

```typescript
// types/dog-stats.ts (핵심 타입만 발췌)

export interface DogStatsResponse {
  dog: DogResponse;
  counselings: CounselingResponse[];
  stats: Stats;
  trainingApplications: TrainingSessionDto[];
  multiCourses: MultiCourseCategoryResponse[];  // ⚠️ 복잡한 중첩 구조!
}

export interface Stats {
  timesApplied: number;
  attendedCount: number;
}

export interface TrainingSessionDto {
  courseId: number;
  courseTitle: string;
  tags: string;
  sessionDate: string;  // YYYY-MM-DD
  sessionStartTime: string;  // HH:mm:ss
  sessionEndTime: string;
}

// ⭐ 다회차 훈련 - 3단계 중첩 구조
export interface MultiCourseCategoryResponse {
  tags: string;  // 1단계: 태그별 그룹
  courses: MultiCourseGroupResponse[];  // 2단계: 코스 배열
}

export interface MultiCourseGroupResponse {
  courseId: number;
  title: string;
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: MultiSessionResponse[];  // 3단계: 세션 배열
}

export interface MultiSessionResponse {
  sessionId: number;
  sessionNo: number;
  sessionDate: string;
  attendanceStatus: 'ATTENDED' | 'ABSENT' | null;
}
```

#### React Query Hook

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

      if (!response.ok) throw new Error('Failed to fetch dog stats');
      return response.json();
    },
    enabled: !!dogId,
    staleTime: 5 * 60 * 1000,
  });
};
```

#### React Component (Statistics Dashboard)

> 📘 **완전한 컴포넌트 예시**는 [상세 문서](./API_DOG_STATS_DETAIL.md#ui-컴포넌트-설계)를 참고하세요.

```typescript
// app/trainer/dogs/[dogId]/stats/page.tsx
'use client';

import { useDogStats } from '@/hooks/useDogStats';
import { useParams } from 'next/navigation';

export default function DogStatsPage() {
  const params = useParams();
  const dogId = Number(params.dogId);
  const { data, isLoading, error } = useDogStats(dogId);

  if (isLoading) return <div>로딩 중...</div>;
  if (error) return <div>오류: {error.message}</div>;
  if (!data) return <div>데이터가 없습니다.</div>;

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 1. 반려견 프로필 */}
      <DogProfileCard dog={data.dog} />

      {/* 2. 통계 요약 */}
      <div className="grid grid-cols-3 gap-4">
        <StatCard
          title="총 신청 횟수"
          value={data.stats.timesApplied}
          icon="📚"
        />
        <StatCard
          title="총 출석 횟수"
          value={data.stats.attendedCount}
          icon="✅"
        />
        <StatCard
          title="출석률"
          value={`${((data.stats.attendedCount / data.stats.timesApplied) * 100).toFixed(1)}%`}
          icon="📊"
        />
      </div>

      {/* 3. 상담 기록 */}
      <CounselingHistory counselings={data.counselings} />

      {/* 4. 단회차 훈련 목록 */}
      <SingleTrainingList trainings={data.trainingApplications} />

      {/* 5. 다회차 훈련 (태그별) ⭐ 복잡! */}
      <MultiCourseCategories categories={data.multiCourses} />
    </div>
  );
}

// 통계 카드 컴포넌트
function StatCard({ title, value, icon }: { title: string; value: string | number; icon: string }) {
  return (
    <div className="bg-white rounded-lg shadow p-4">
      <div className="text-2xl mb-2">{icon}</div>
      <div className="text-2xl font-bold">{value}</div>
      <div className="text-sm text-gray-600">{title}</div>
    </div>
  );
}

// 다회차 훈련 카테고리 (중첩 구조 처리)
function MultiCourseCategories({ categories }: { categories: MultiCourseCategoryResponse[] }) {
  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold">📚 다회차 훈련 이력</h2>
      
      {categories.map((category) => (
        <div key={category.tags} className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-bold mb-4">{category.tags}</h3>
          
          {/* 코스 목록 */}
          {category.courses.map((course) => (
            <div key={course.courseId} className="border-l-4 border-blue-500 pl-4 mb-4">
              <h4 className="font-semibold">{course.title}</h4>
              <p className="text-sm text-gray-600">
                출석: {course.attendedSessions}/{course.totalSessions} ({course.attendanceRate.toFixed(1)}%)
              </p>
              
              {/* 세션 목록 */}
              <div className="mt-2 space-y-1">
                {course.sessions.map((session) => (
                  <div key={session.sessionId} className="text-sm flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${
                      session.attendanceStatus === 'ATTENDED' ? 'bg-green-500' :
                      session.attendanceStatus === 'ABSENT' ? 'bg-red-500' : 'bg-gray-300'
                    }`} />
                    <span>{session.sessionNo}회차 - {session.sessionDate}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
```

### ⚠️ 핵심 주의사항

#### 1️⃣ **multiCourses 데이터 구조 이해**

```typescript
// ❌ 잘못된 접근
data.multiCourses.map(course => ...)  // 틀림!

// ✅ 올바른 접근 (3단계 중첩)
data.multiCourses.map(category =>           // 1단계: 태그별 그룹
  category.courses.map(course =>             // 2단계: 코스
    course.sessions.map(session => ...)      // 3단계: 세션
  )
)
```

#### 2️⃣ **attendanceStatus null 처리**

```typescript
// 예정된 세션은 attendanceStatus가 null
session.attendanceStatus === 'ATTENDED'  // ✅ 출석
session.attendanceStatus === 'ABSENT'    // ✅ 결석
session.attendanceStatus === null        // ✅ 예정
```

#### 3️⃣ **출석률 계산**

```typescript
// stats에는 통계만, 출석률은 직접 계산
const attendanceRate = data.stats.timesApplied > 0
  ? (data.stats.attendedCount / data.stats.timesApplied) * 100
  : 0;
```

#### 4️⃣ **빈 배열 체크**

```typescript
// 모든 배열은 비어있을 수 있음
if (data.counselings.length === 0) {
  return <EmptyState message="상담 기록이 없습니다" />;
}
```

### 📚 추가 리소스

- **📖 상세 문서**: [API_DOG_STATS_DETAIL.md](./API_DOG_STATS_DETAIL.md)
  - 완전한 TypeScript 인터페이스
  - 실제 응답 예시 (전체)
  - 5가지 UI 컴포넌트 전체 코드
  - 트러블슈팅 가이드
  - 데이터 시각화 예시

---

## 🔐 인증 (Authentication)

모든 API는 JWT 토큰 기반 인증이 필요합니다.

### 토큰 저장 방법
```typescript
// 로그인 후
localStorage.setItem('accessToken', token);

// 또는 쿠키 사용 (더 안전)
document.cookie = `accessToken=${token}; path=/; secure; httpOnly`;
```

### Axios Interceptor 설정 (권장)
```typescript
// lib/axios.ts
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터 (토큰 만료 처리)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // 토큰 만료 시 재발급 또는 로그아웃 처리
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

// 사용
import api from '@/lib/axios';

const getTrainerUsers = () => api.get('/api/trainer/users/0');
```

---

## 🚀 사용 흐름 (User Flow)

### 시나리오: 회원의 반려견 통계 조회

```typescript
// 1. 훈련사가 관리하는 회원 목록 조회
const users = await getTrainerUsers();

// 2. 특정 회원 선택 후 반려견 목록 조회
const dogs = await getUserDogs(selectedUser.userId);

// 3. 특정 반려견 선택 후 통계 조회
const stats = await getDogStats(selectedDog.dogId);
```

### 전체 플로우 컴포넌트
```typescript
// app/trainer/dashboard/page.tsx
'use client';

import { useState } from 'react';

export default function TrainerDashboard() {
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [selectedDogId, setSelectedDogId] = useState<number | null>(null);

  return (
    <div className="grid grid-cols-3 gap-4">
      {/* 1단계: 회원 목록 */}
      <div>
        <h2>내 회원 목록</h2>
        <UserList onSelectUser={setSelectedUserId} />
      </div>

      {/* 2단계: 반려견 목록 */}
      {selectedUserId && (
        <div>
          <h2>반려견 목록</h2>
          <DogList userId={selectedUserId} onSelectDog={setSelectedDogId} />
        </div>
      )}

      {/* 3단계: 통계 */}
      {selectedDogId && (
        <div>
          <h2>반려견 통계</h2>
          <DogStats dogId={selectedDogId} />
        </div>
      )}
    </div>
  );
}
```

---

## ⚠️ 주의사항

### 1. 인증 방식
- 모든 API는 JWT 토큰을 사용하여 훈련사 인증을 수행합니다.
- `GET /api/trainer/users`는 URL에 trainerId가 없으며, JWT 토큰에서 자동으로 추출합니다.

### 2. 권한 검증
- 모든 API는 서버에서 JWT 토큰을 검증하여 훈련사 권한을 확인합니다.
- 다른 훈련사의 데이터에는 접근할 수 없습니다.

### 3. 프로필 이미지 처리 ⭐
- **모든 `profileImage` 필드는 S3 Presigned URL로 반환됩니다.**
- **유효기간**: 15분 (900초)
- **만료 후**: 이미지 로드 실패 시 데이터를 다시 fetch해야 합니다.
- **null 처리**: 이미지가 없는 경우 `null`이 반환되므로 조건부 렌더링 필수
- **캐싱**: React Query 사용 시 `staleTime`을 5분 이하로 설정 권장

### 4. 에러 처리
- 401: 토큰 만료 → 재로그인 필요
- 403: 권한 없음 → 접근 권한 확인
- 404: 리소스 없음 → 존재하지 않는 데이터

### 5. 성능 최적화
- React Query 사용 시 적절한 `staleTime`과 `cacheTime` 설정 권장
- 서버 컴포넌트를 활용하여 초기 로딩 성능 개선
- 무한 스크롤이 필요한 경우 페이지네이션 API 추가 요청 고려
- Presigned URL 만료를 고려하여 10분마다 자동 갱신(`refetchInterval`) 권장

---

## 📚 추가 리소스

### API 테스트
```bash
# cURL 예시 - 회원 목록 조회
curl -X GET "http://localhost:8080/api/trainer/users" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# cURL 예시 - 반려견 목록 조회
curl -X GET "http://localhost:8080/api/trainer/dogs/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# cURL 예시 - 반려견 통계 조회
curl -X GET "http://localhost:8080/api/trainer/user/dogs/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Postman Collection
프로젝트 루트의 `postman/trainer-api.json` 파일을 import하여 사용하세요.

---

## 🖼️ 프로필 이미지 처리 가이드

### 📌 개요
모든 프로필 이미지(`profileImage`)는 **S3 Presigned URL**로 제공되며, 프론트엔드에서 바로 사용할 수 있습니다.

### 🔑 주요 특징

#### 1. **자동 URL 변환**
```
DB 저장값:     "user-profiles/123/profile.jpg"
API 응답값:    "https://mungtrainer-s3.s3.ap-northeast-2.amazonaws.com/user-profiles/123/profile.jpg?X-Amz-..."
```

백엔드에서 자동으로 S3 키를 Presigned URL로 변환하므로, 프론트엔드는 추가 처리 없이 바로 사용 가능합니다.

#### 2. **유효기간: 15분**
- Presigned URL은 생성 시점부터 **15분(900초)** 동안만 유효합니다.
- 15분 이후에는 403 Forbidden 에러가 발생합니다.

#### 3. **null 처리**
- 프로필 이미지가 없는 경우 `null`이 반환됩니다.
- 조건부 렌더링으로 기본 이미지를 표시하세요.

### 💻 프론트엔드 구현 방법

#### **1. 기본 HTML img 태그**
```typescript
{user.profileImage ? (
  <img src={user.profileImage} alt={user.name} />
) : (
  <div className="default-avatar">👤</div>
)}
```

#### **2. Next.js Image 컴포넌트**
```typescript
import Image from 'next/image';

// 방법 1: unoptimized 사용
<Image
  src={user.profileImage || '/default-avatar.png'}
  alt={user.name}
  width={100}
  height={100}
  unoptimized
/>

// 방법 2: next.config.js 설정
// next.config.js
module.exports = {
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'mungtrainer-s3.s3.ap-northeast-2.amazonaws.com',
      },
    ],
  },
};
```

#### **3. React Query로 자동 갱신**
```typescript
import { useQuery } from '@tanstack/react-query';

export const useTrainerUsers = () => {
  return useQuery({
    queryKey: ['trainerUsers'],
    queryFn: fetchTrainerUsers,
    staleTime: 5 * 60 * 1000,      // 5분 후 stale 상태
    refetchInterval: 10 * 60 * 1000, // 10분마다 자동 갱신
  });
};
```

### ⚠️ 주의사항

#### **1. URL 만료 처리**
```typescript
// 이미지 로드 실패 시 재시도
const [imageError, setImageError] = useState(false);

<img
  src={user.profileImage}
  alt={user.name}
  onError={() => {
    if (!imageError) {
      setImageError(true);
      refetch(); // 데이터 다시 가져오기
    }
  }}
/>
```

#### **2. 캐싱 전략**
```typescript
// ❌ 잘못된 방법: 15분 이상 캐싱
const { data } = useQuery({
  staleTime: 30 * 60 * 1000, // 30분 - URL이 만료될 수 있음!
});

// ✅ 올바른 방법: 15분 이내로 캐싱
const { data } = useQuery({
  staleTime: 5 * 60 * 1000,  // 5분
});
```

#### **3. SSR/SSG 사용 시**
```typescript
// Server Component에서 사용 시
async function UserProfile({ userId }: Props) {
  const user = await fetchUser(userId);
  
  // Presigned URL은 서버에서 생성되므로 클라이언트에 전달됨
  return (
    <img src={user.profileImage} alt={user.name} />
  );
}
```

### 🎨 UI 패턴 예시

#### **1. 프로필 카드**
```typescript
function UserCard({ user }: { user: TrainerUserListResponse }) {
  return (
    <div className="flex items-center gap-4 p-4 border rounded-lg">
      {user.profileImage ? (
        <img
          src={user.profileImage}
          alt={user.name}
          className="w-16 h-16 rounded-full object-cover"
        />
      ) : (
        <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center">
          <span className="text-2xl">👤</span>
        </div>
      )}
      <div>
        <h3 className="font-bold">{user.name}</h3>
        <p className="text-sm text-gray-600">{user.email}</p>
      </div>
    </div>
  );
}
```

#### **2. 반려견 갤러리**
```typescript
function DogGallery({ dogs }: { dogs: DogResponse[] }) {
  return (
    <div className="grid grid-cols-3 gap-4">
      {dogs.map((dog) => (
        <div key={dog.dogId} className="relative aspect-square">
          {dog.profileImage ? (
            <img
              src={dog.profileImage}
              alt={dog.dogName}
              className="w-full h-full object-cover rounded-lg"
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-blue-100 to-purple-100 rounded-lg flex items-center justify-center">
              <span className="text-6xl">🐕</span>
            </div>
          )}
          <div className="absolute bottom-0 left-0 right-0 bg-black/50 text-white p-2 rounded-b-lg">
            <p className="font-semibold">{dog.dogName}</p>
          </div>
        </div>
      ))}
    </div>
  );
}
```

### 🔧 트러블슈팅

#### **문제 1: 이미지가 안 보임**
```
원인: Presigned URL이 만료됨 (15분 경과)
해결: 데이터를 다시 fetch하거나 refetch 호출
```

#### **문제 2: CORS 에러**
```
원인: S3 버킷의 CORS 설정 문제
해결: 백엔드 팀에 문의 (S3 설정 확인 필요)
```

#### **문제 3: Next.js Image 최적화 에러**
```
원인: S3 도메인이 허용되지 않음
해결: unoptimized 사용 또는 next.config.js에 도메인 추가
```

### 📊 API 별 이미지 필드 정리

| API | 이미지 필드명 | 설명 |
|-----|-------------|------|
| `GET /api/trainer/users` | `profileImage` | 회원 프로필 이미지 |
| `GET /api/trainer/dogs/{userId}` | `profileImage` | 반려견 프로필 이미지 |
| `GET /api/trainer/user/dogs/{dogId}` | `imageUrl` | 반려견 통계 페이지 이미지 |

> 💡 **일관성**: 모든 이미지 필드는 동일한 방식(S3 Presigned URL)으로 처리됩니다.

---

## 📞 문의

- 백엔드 API 관련: [백엔드 팀]
- 프론트엔드 구현 관련: [프론트엔드 팀]

---

**마지막 업데이트**: 2024-12-23
**API 버전**: v1.0

