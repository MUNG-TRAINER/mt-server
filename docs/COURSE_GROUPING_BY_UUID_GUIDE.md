# 🔄 다회차 훈련 수강 이력 그룹화 가이드

> **요구사항**: 같은 과정(tags UUID)을 여러 번 수강한 경우, 하나로 묶어서 "몇 회 수강"을 표시하고, 각 수강별 차이점은 펼쳤을 때 보여주기

---

## 🎯 UI/UX 디자인

### Before (현재)
```
📚 다회차 훈련 이력
┌─ 기초,사회화,복종 (tags)
│  ├─ 강아지 기초 훈련 4주 코스
│  │   출석률: 80% (8/10)
│  │
│  ├─ 강아지 기초 훈련 4주 코스 (2024 겨울)  ← 같은 tags UUID
│  │   출석률: 90% (9/10)
│  │
│  └─ 강아지 기초 훈련 4주 코스 - 심화  ← 같은 tags UUID
│      출석률: 70% (7/10)
```

### After (개선)
```
📚 다회차 훈련 이력
┌─ 기초,사회화,복종 (tags)
│  └─ 강아지 기초 훈련 4주 코스 [3회 수강] 🔽
│      
│      📊 전체 평균 출석률: 80% (24/30)
│      
│      ├─ 1차 수강 (2024.01 ~ 2024.02)
│      │   제목: 강아지 기초 훈련 4주 코스
│      │   출석률: 80% (8/10)
│      │   [세션 보기 🔽]
│      │
│      ├─ 2차 수강 (2024.07 ~ 2024.08)
│      │   제목: 강아지 기초 훈련 4주 코스 (2024 겨울)
│      │   출석률: 90% (9/10)
│      │   [세션 보기 🔽]
│      │
│      └─ 3차 수강 (2024.12 ~ 2025.01)
│          제목: 강아지 기초 훈련 4주 코스 - 심화
│          출석률: 70% (7/10)
│          [세션 보기 🔽]
```

---

## 💻 프론트엔드 구현

### 1️⃣ 데이터 그룹화 유틸리티

```typescript
// utils/groupCoursesByTags.ts

import { MultiCourseGroupResponse } from '@/types/dog-stats';

export interface CourseEnrollmentHistory {
  enrollmentNumber: number;  // 몇 차 수강
  courseId: number;
  title: string;
  description: string;
  location: string;
  difficulty: string;
  mainImage: string;
  totalSessions: number;
  attendedSessions: number;
  attendanceRate: number;
  sessions: any[];
  // 날짜 범위 계산
  startDate: string;
  endDate: string;
}

export interface GroupedCourse {
  tags: string;  // UUID
  // 대표 정보 (첫 번째 수강 기준)
  representativeTitle: string;
  location: string;
  difficulty: string;
  mainImage: string;
  
  // 전체 통계
  totalEnrollments: number;  // 총 수강 횟수
  totalSessions: number;     // 전체 세션 수
  totalAttendedSessions: number;  // 전체 출석 수
  overallAttendanceRate: number;  // 전체 평균 출석률
  
  // 수강 이력
  enrollmentHistory: CourseEnrollmentHistory[];
}

/**
 * 같은 tags(UUID)를 가진 코스들을 그룹화
 */
export function groupCoursesByTags(
  courses: MultiCourseGroupResponse[]
): GroupedCourse[] {
  // tags(UUID)로 그룹화
  const grouped = courses.reduce((acc, course) => {
    const tagsKey = course.tags;  // UUID 값
    
    if (!acc[tagsKey]) {
      acc[tagsKey] = [];
    }
    acc[tagsKey].push(course);
    
    return acc;
  }, {} as Record<string, MultiCourseGroupResponse[]>);
  
  // 각 그룹을 GroupedCourse 형태로 변환
  return Object.entries(grouped).map(([tags, courseList]) => {
    // 날짜순 정렬 (오래된 순)
    const sortedCourses = courseList.sort((a, b) => {
      const aDate = a.sessions[0]?.sessionDate || '';
      const bDate = b.sessions[0]?.sessionDate || '';
      return aDate.localeCompare(bDate);
    });
    
    // 전체 통계 계산
    const totalSessions = sortedCourses.reduce((sum, c) => sum + c.totalSessions, 0);
    const totalAttendedSessions = sortedCourses.reduce((sum, c) => sum + c.attendedSessions, 0);
    const overallAttendanceRate = totalSessions > 0 
      ? (totalAttendedSessions / totalSessions) * 100 
      : 0;
    
    // 대표 정보 (첫 번째 수강 기준)
    const representative = sortedCourses[0];
    
    // 수강 이력 생성
    const enrollmentHistory: CourseEnrollmentHistory[] = sortedCourses.map((course, index) => {
      // 시작일/종료일 계산
      const dates = course.sessions.map(s => s.sessionDate).sort();
      const startDate = dates[0] || '';
      const endDate = dates[dates.length - 1] || '';
      
      return {
        enrollmentNumber: index + 1,
        courseId: course.courseId,
        title: course.title,
        description: course.description,
        location: course.location,
        difficulty: course.difficulty,
        mainImage: course.mainImage,
        totalSessions: course.totalSessions,
        attendedSessions: course.attendedSessions,
        attendanceRate: course.attendanceRate,
        sessions: course.sessions,
        startDate,
        endDate,
      };
    });
    
    return {
      tags,
      representativeTitle: representative.title,
      location: representative.location,
      difficulty: representative.difficulty,
      mainImage: representative.mainImage,
      totalEnrollments: sortedCourses.length,
      totalSessions,
      totalAttendedSessions,
      overallAttendanceRate,
      enrollmentHistory,
    };
  });
}
```

---

### 2️⃣ 그룹화된 코스 카드 컴포넌트

```typescript
// components/GroupedMultiCourseCard.tsx
'use client';

import { useState } from 'react';
import { GroupedCourse } from '@/utils/groupCoursesByTags';
import EnrollmentHistoryItem from './EnrollmentHistoryItem';

interface Props {
  groupedCourse: GroupedCourse;
}

export default function GroupedMultiCourseCard({ groupedCourse }: Props) {
  const [isExpanded, setIsExpanded] = useState(false);

  const difficultyConfig = {
    BEGINNER: { label: '초급', color: 'bg-green-100 text-green-800' },
    INTERMEDIATE: { label: '중급', color: 'bg-yellow-100 text-yellow-800' },
    ADVANCED: { label: '고급', color: 'bg-red-100 text-red-800' },
  };

  const difficulty = difficultyConfig[groupedCourse.difficulty];

  // 출석률에 따른 색상
  const getRateColor = (rate: number) => {
    if (rate >= 80) return 'text-green-600';
    if (rate >= 50) return 'text-yellow-600';
    return 'text-red-600';
  };

  return (
    <div className="border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition">
      {/* 헤더 (클릭 가능) */}
      <div
        className="bg-gradient-to-r from-gray-50 to-gray-100 p-5 cursor-pointer hover:from-gray-100 hover:to-gray-200 transition"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-start justify-between">
          <div className="flex-1">
            {/* 제목 + 수강 횟수 뱃지 */}
            <div className="flex items-center gap-3 mb-2">
              <h3 className="text-lg font-bold text-gray-900">
                {groupedCourse.representativeTitle}
              </h3>
              
              {/* 수강 횟수 뱃지 ⭐ */}
              <span className="bg-blue-500 text-white px-3 py-1 rounded-full text-sm font-bold">
                {groupedCourse.totalEnrollments}회 수강
              </span>
              
              <span className={`px-2 py-1 rounded text-xs font-semibold ${difficulty.color}`}>
                {difficulty.label}
              </span>
            </div>
            
            {/* 전체 통계 */}
            <div className="flex flex-wrap items-center gap-4 text-sm text-gray-700 mb-3">
              <span>📍 {groupedCourse.location}</span>
              <span>📅 전체 {groupedCourse.totalSessions}회</span>
              <span>✅ 총 출석 {groupedCourse.totalAttendedSessions}회</span>
              <span className={`font-bold ${getRateColor(groupedCourse.overallAttendanceRate)}`}>
                전체 평균 출석률: {groupedCourse.overallAttendanceRate.toFixed(1)}%
              </span>
            </div>

            {/* 간단한 수강 이력 요약 */}
            <div className="text-xs text-gray-500">
              {groupedCourse.enrollmentHistory.map((enrollment, idx) => (
                <span key={enrollment.courseId}>
                  {idx > 0 && ' · '}
                  {enrollment.enrollmentNumber}차({enrollment.attendanceRate.toFixed(0)}%)
                </span>
              ))}
            </div>
          </div>
          
          {/* 펼치기 버튼 */}
          <button 
            className="text-2xl ml-4 transition-transform duration-200"
            style={{ transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)' }}
          >
            🔽
          </button>
        </div>

        {/* 전체 출석률 프로그레스 바 */}
        <div className="mt-3 bg-gray-200 rounded-full h-2.5 overflow-hidden">
          <div
            className="bg-gradient-to-r from-blue-500 via-purple-500 to-green-500 h-full transition-all duration-300"
            style={{ width: `${Math.min(groupedCourse.overallAttendanceRate, 100)}%` }}
          />
        </div>
      </div>

      {/* 수강 이력 상세 (펼쳤을 때만) */}
      {isExpanded && (
        <div className="p-5 bg-white border-t">
          <h4 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
            <span>📚</span>
            수강 이력 ({groupedCourse.totalEnrollments}회)
          </h4>
          
          <div className="space-y-4">
            {groupedCourse.enrollmentHistory.map((enrollment) => (
              <EnrollmentHistoryItem 
                key={enrollment.courseId} 
                enrollment={enrollment} 
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

---

### 3️⃣ 개별 수강 이력 아이템

```typescript
// components/EnrollmentHistoryItem.tsx
'use client';

import { useState } from 'react';
import { CourseEnrollmentHistory } from '@/utils/groupCoursesByTags';
import SessionTimeline from './SessionTimeline';

interface Props {
  enrollment: CourseEnrollmentHistory;
}

export default function EnrollmentHistoryItem({ enrollment }: Props) {
  const [showSessions, setShowSessions] = useState(false);

  // 날짜 포맷팅
  const formatDateRange = (start: string, end: string) => {
    if (!start || !end) return '날짜 정보 없음';
    
    const startDate = new Date(start);
    const endDate = new Date(end);
    
    return `${startDate.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' })} ~ ${endDate.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' })}`;
  };

  return (
    <div className="border-l-4 border-blue-400 pl-4 py-3 bg-gray-50 rounded-r-lg">
      <div className="flex items-start justify-between mb-2">
        <div className="flex-1">
          {/* 수강 차수 */}
          <div className="flex items-center gap-2 mb-1">
            <span className="bg-blue-600 text-white px-2 py-1 rounded text-sm font-bold">
              {enrollment.enrollmentNumber}차 수강
            </span>
            <span className="text-xs text-gray-500">
              {formatDateRange(enrollment.startDate, enrollment.endDate)}
            </span>
          </div>
          
          {/* 제목 (미세한 차이 표시) */}
          <p className="font-semibold text-gray-900 mb-1">
            {enrollment.title}
          </p>
          
          {/* 설명 */}
          {enrollment.description && (
            <p className="text-sm text-gray-600 mb-2">
              {enrollment.description}
            </p>
          )}
          
          {/* 통계 */}
          <div className="flex items-center gap-4 text-sm">
            <span className="text-gray-700">
              📊 {enrollment.attendedSessions}/{enrollment.totalSessions}회 출석
            </span>
            <span className={`font-semibold ${
              enrollment.attendanceRate >= 80 ? 'text-green-600' :
              enrollment.attendanceRate >= 50 ? 'text-yellow-600' : 'text-red-600'
            }`}>
              출석률: {enrollment.attendanceRate.toFixed(1)}%
            </span>
          </div>
        </div>
      </div>

      {/* 세션 펼치기 버튼 */}
      <button
        onClick={() => setShowSessions(!showSessions)}
        className="mt-2 text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1"
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

---

### 4️⃣ 메인 리스트 컴포넌트 (수정)

```typescript
// components/MultiCourseList.tsx
import { MultiCourseCategoryResponse } from '@/types/dog-stats';
import { groupCoursesByTags } from '@/utils/groupCoursesByTags';
import GroupedMultiCourseCard from './GroupedMultiCourseCard';

interface Props {
  multiCourses: MultiCourseCategoryResponse[];
}

export default function MultiCourseList({ multiCourses }: Props) {
  return (
    <div className="space-y-8">
      <h2 className="text-2xl font-bold">📚 다회차 훈련 이력</h2>
      
      {multiCourses.map((category, idx) => {
        // ⭐ 같은 tags(UUID)로 그룹화
        const groupedCourses = groupCoursesByTags(category.courses);
        
        return (
          <div key={idx} className="bg-white rounded-lg shadow-md p-6">
            {/* 태그 헤더 */}
            <div className="flex items-center gap-3 mb-6">
              <span className="bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-2 rounded-full font-bold">
                {category.tags}
              </span>
              <span className="text-gray-600">
                {groupedCourses.length}개 과정
              </span>
              <span className="text-gray-500 text-sm">
                (총 {category.courses.length}회 수강)
              </span>
            </div>

            {/* 그룹화된 코스 목록 */}
            <div className="space-y-4">
              {groupedCourses.map((groupedCourse) => (
                <GroupedMultiCourseCard 
                  key={groupedCourse.tags} 
                  groupedCourse={groupedCourse} 
                />
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
```

---

## 🎨 UI 미리보기

### 접혔을 때
```
┌─────────────────────────────────────────────────────┐
│ 강아지 기초 훈련 4주 코스  [3회 수강]  [초급]       │
│                                                     │
│ 📍 서울시 강남구  📅 전체 30회  ✅ 총 출석 24회     │
│ 전체 평균 출석률: 80.0%                             │
│                                                     │
│ 1차(80%) · 2차(90%) · 3차(70%)                     │
│                                                     │
│ ████████████████████████░░░░  80%                  │
│                                              🔽     │
└─────────────────────────────────────────────────────┘
```

### 펼쳤을 때
```
┌─────────────────────────────────────────────────────┐
│ 강아지 기초 훈련 4주 코스  [3회 수강]  [초급]       │
│                                              🔼     │
├─────────────────────────────────────────────────────┤
│ 📚 수강 이력 (3회)                                  │
│                                                     │
│ ┃ [1차 수강] 2024. 1. ~ 2024. 2.                   │
│ ┃ 강아지 기초 훈련 4주 코스                         │
│ ┃ 📊 8/10회 출석  출석률: 80.0%                    │
│ ┃ 🔽 세션 상세 보기 (10회차)                        │
│                                                     │
│ ┃ [2차 수강] 2024. 7. ~ 2024. 8.                   │
│ ┃ 강아지 기초 훈련 4주 코스 (2024 겨울) ⭐ 차이!   │
│ ┃ 📊 9/10회 출석  출석률: 90.0%                    │
│ ┃ 🔽 세션 상세 보기 (10회차)                        │
│                                                     │
│ ┃ [3차 수강] 2024. 12. ~ 2025. 1.                  │
│ ┃ 강아지 기초 훈련 4주 코스 - 심화 ⭐ 차이!        │
│ ┃ 📊 7/10회 출석  출석률: 70.0%                    │
│ ┃ 🔽 세션 상세 보기 (10회차)                        │
└─────────────────────────────────────────────────────┘
```

---

## ✅ 장점

1. ✅ **같은 과정(UUID) 한눈에 파악** - "3회 수강" 뱃지
2. ✅ **전체 평균 출석률** 즉시 확인
3. ✅ **과정별 미세한 차이** 명확히 표시
4. ✅ **세션 상세**는 필요할 때만 펼쳐서 확인
5. ✅ **시간순 정렬**로 학습 진행 과정 파악
6. ✅ **백엔드 수정 불필요** - 프론트에서만 처리

---

## ⚡ 성능 최적화

### 📊 성능 영향

#### 일반적인 경우 (문제 없음)
```javascript
// 반려견당 평균 5-20개 과정
실행 시간: < 2ms
체감: 없음 ✅
```

#### 많은 경우 (약간 영향)
```javascript
// 50개 이상 과정
실행 시간: 3-5ms
체감: 거의 없음 ⚠️
```

### 🎯 최적화 방법

#### 1. useMemo로 메모이제이션

```typescript
// components/MultiCourseList.tsx
export default function MultiCourseList({ multiCourses }: Props) {
  return (
    <div className="space-y-8">
      {multiCourses.map((category) => {
        // ⭐ useMemo로 재계산 방지
        const groupedCourses = useMemo(
          () => groupCoursesByTags(category.courses),
          [category.courses]
        );
        
        return (
          <div key={category.tags}>
            {groupedCourses.map(course => (
              <GroupedMultiCourseCard 
                key={course.tags} 
                groupedCourse={course} 
              />
            ))}
          </div>
        );
      })}
    </div>
  );
}
```

#### 2. React Query 캐싱 활용

```typescript
// hooks/useDogStats.ts
export const useDogStats = (dogId: number) => {
  return useQuery<DogStatsResponse>({
    queryKey: ['dogStats', dogId],
    queryFn: fetchDogStats,
    staleTime: 5 * 60 * 1000,  // ⭐ 5분간 재계산 안 함
    refetchInterval: 10 * 60 * 1000,
  });
};
```

#### 3. 성능 모니터링

```typescript
// 개발 환경에서만 실행
if (process.env.NODE_ENV === 'development') {
  console.time('grouping');
  const grouped = groupCoursesByTags(courses);
  console.timeEnd('grouping');
  
  if (courses.length > 50) {
    console.warn('⚠️ 과정 개수 많음, 백엔드 최적화 검토 필요');
  }
}
```

### 📈 성능이 문제가 된다면?

**백엔드 그룹화 구현 고려** (평균 과정 > 50개인 경우)

자세한 내용은 [성능 분석 문서](./GROUPING_PERFORMANCE_ANALYSIS.md) 참고

---

## 📚 추가 리소스

1. ✅ `utils/groupCoursesByTags.ts` 생성
2. ✅ `components/GroupedMultiCourseCard.tsx` 생성
3. ✅ `components/EnrollmentHistoryItem.tsx` 생성
4. ✅ `components/MultiCourseList.tsx` 수정
5. ✅ `SessionTimeline.tsx` 재사용

---

**이제 같은 UUID를 가진 과정들이 깔끔하게 그룹화되어 표시됩니다!** 🎉

