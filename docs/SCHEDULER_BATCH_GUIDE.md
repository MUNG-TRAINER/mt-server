# 🚀 훈련 과정 자동화 시스템 가이드

## 📌 개요

이 문서는 멍트레이너 프로젝트의 **스케줄러 기반 배치 처리** 및 **대기열 시스템**에 대한 설명입니다.
스터디 발표를 위해 작성되었으며, 구현된 기능의 흐름과 기술 스택을 이해하기 쉽게 정리했습니다.

---

## 📚 목차

1. [왜 스케줄러를 도입했나요?](#1-왜-스케줄러를-도입했나요)
2. [전체 시스템 구조](#2-전체-시스템-구조)
3. [3가지 스케줄러 상세 설명](#3-3가지-스케줄러-상세-설명)
4. [대기열 시스템](#4-대기열-시스템)
5. [기술 스택](#5-기술-스택)
6. [핵심 기술 포인트](#6-핵심-기술-포인트)

---

## 1. 왜 스케줄러를 도입했나요?

### 🤔 문제 상황

강아지 훈련 과정 신청 시스템에서 다음과 같은 문제가 발생했습니다:

1. **수동 관리의 한계**
   - 신청 기한이 지났는데 수동으로 상태를 바꿔야 함
   - 결제하지 않은 사용자를 일일이 확인해야 함
   - 훈련 과정이 끝났는데 상태가 "진행중"으로 남아있음

2. **사용자 경험 저하**
   - 정원이 차면 대기해야 하는데, 자리가 나도 알림이 없음
   - 결제 기한을 놓치는 경우 발생

3. **관리자의 부담**
   - 24시간 모니터링 필요
   - 반복적인 수동 작업

### ✅ 해결 방법: 자동화

**스케줄러를 통해 주기적으로 상태를 자동으로 변경**하여 문제를 해결했습니다.

---

## 2. 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Scheduler                     │
│                (@EnableScheduling)                      │
└─────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ 수업 시작      │  │ 결제 기한      │  │ 훈련 과정      │
│ 마감 스케줄러  │  │ 만료 스케줄러  │  │ 상태 스케줄러  │
│               │  │               │  │               │
│ 매 10분       │  │ 매 10분       │  │ 매 10분       │
│ (0, 10, 20...)│  │ (3, 13, 23...)│  │ (6, 16, 26...)│
└───────────────┘  └───────────────┘  └───────────────┘
        │                  │                  │
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ 미승인 신청    │  │ 미결제 신청    │  │ 세션/과정      │
│ EXPIRED 처리  │  │ EXPIRED 처리  │  │ 상태 업데이트  │
└───────────────┘  │               │  └───────────────┘
                   │ 대기자 자동 승격│
                   └───────┬───────┘
                           │
                           ▼
                   ┌───────────────┐
                   │  대기열 시스템 │
                   │  (Waiting)    │
                   └───────────────┘
```

### 실행 시간 분산 전략

각 스케줄러가 동시에 실행되면 DB 부하가 발생할 수 있으므로, **시간을 분산**시켰습니다:

- **SessionDeadlineScheduler**: 0분, 10분, 20분, 30분, 40분, 50분
- **PaymentDeadlineScheduler**: 3분, 13분, 23분, 33분, 43분, 53분
- **CourseStatusScheduler**: 6분, 16분, 26분, 36분, 46분, 56분

---

## 3. 3가지 스케줄러 상세 설명

### 📅 스케줄러 1: 수업 시작 마감 스케줄러 (SessionDeadlineScheduler)

#### 목적
수업 시작 24시간 전까지 승인되지 않은 신청을 자동으로 마감 처리

#### 실행 주기
- **10분마다** (cron: `0 0/10 * * * *`)
- 예시: 9:00, 9:10, 9:20, 9:30...

#### 처리 로직
```
1. 현재 시간으로부터 24시간 후에 시작하는 세션 조회
2. 해당 세션의 미승인 신청 조회
   - 대상: APPLIED, COUNSELING_REQUIRED, WAITING 상태
   - 제외: ACCEPT, PAID 상태 (이미 승인된 신청)
3. 모든 미승인 신청을 EXPIRED 상태로 변경
```

#### 코드 흐름
```java
@Scheduled(cron = "0 0/10 * * * *")
public void processSessionDeadline() {
    // 1. 기능 비활성화 체크 (긴급 롤백용)
    if (!sessionDeadlineEnabled) return;
    
    // 2. 서비스 호출 (트랜잭션 분리)
    sessionDeadlineService.processSessionDeadline(24);
}
```

#### 왜 별도 Service로 분리했나요?
- **Self-invocation 문제 해결**: 같은 클래스 내에서 `@Transactional` 호출 시 트랜잭션이 동작하지 않음
- **트랜잭션 관리**: 예외 발생 시 롤백 보장
- **테스트 용이성**: 비즈니스 로직만 단위 테스트 가능

---

### 💳 스케줄러 2: 결제 기한 만료 스케줄러 (PaymentDeadlineScheduler)

#### 목적
ACCEPT 상태로 승인되었지만 결제하지 않은 신청을 만료 처리하고, 대기자를 자동 승격

#### 실행 주기
- **10분마다** (cron: `0 3/10 * * * *`)
- 예시: 9:03, 9:13, 9:23, 9:33...

#### 처리 로직
```
1. 승인 후 24시간이 지난 ACCEPT 상태 신청 조회
2. EXPIRED 상태로 변경
3. 해당 세션의 다음 대기자 자동 승격
   - 정원 확인 (현재 인원 < 최대 인원)
   - 가장 오래 대기한 사람 조회
   - WAITING → ACCEPT 상태로 변경
   - 출석 정보 생성
   - 결제 기한 설정 (24시간)
```

#### 핵심 코드
```java
@Scheduled(cron = "0 3/10 * * * *")
@Transactional
public void processExpiredPayments() {
    // 1. 만료된 신청 조회
    List<Long> expiredIds = dao.findExpiredAcceptApplications();
    
    // 2. 각 신청 처리
    for (Long appId : expiredIds) {
        // EXPIRED 처리
        dao.expireApplication(appId);
        
        // 대기자 승격
        promoteNextWaiting(sessionId);
    }
}
```

---

### 📊 스케줄러 3: 훈련 과정 상태 스케줄러 (CourseStatusScheduler)

#### 목적
훈련 과정과 세션의 상태를 실시간으로 업데이트

#### 실행 주기
- **10분마다** (cron: `0 6/10 * * * *`)
- 예시: 9:06, 9:16, 9:26, 9:36...

#### 처리 로직

##### 1단계: 세션 종료 처리
```
- 종료 시간이 지난 세션 조회
- 상태를 DONE으로 변경
```

##### 2단계: 과정 진행 중 처리
```
- SCHEDULED 상태의 과정 중 첫 세션이 시작된 과정 조회
- 상태를 IN_PROGRESS로 변경
- 이 상태의 과정은 더 이상 신청 불가
```

##### 3단계: 과정 완료 처리
```
- IN_PROGRESS 상태의 과정 중 모든 세션이 종료된 과정 조회
- 상태를 DONE으로 변경
```

#### 상태 흐름도
```
SCHEDULED → IN_PROGRESS → DONE
   (예정)      (진행중)      (완료)
     │            │            │
     │            │            │
첫 세션 시작   모든 세션 종료   -
```

---

## 4. 대기열 시스템

### 🎯 대기열이란?

정원이 가득 찬 수업에 대해 **대기 순번을 부여**하고, 자리가 나면 **자동으로 승격**시키는 시스템

### 대기열 상태 흐름

```
사용자 신청
    │
    ▼
정원 확인
    │
    ├─ 정원 남음 → APPLIED (일반 신청)
    │
    └─ 정원 초과 → WAITING (대기 상태)
                      │
                      ├─ waiting.status = WAITING
                      ├─ training_course_application.status = WAITING
                      │
                      ▼
                 훈련사 미리 승인 (선택)
                      │
                      ├─ waiting.is_approved = true
                      │
                      ▼
              자리 발생 대기 (WAITING 유지)
                      │
                      ▼
           자리 발생 (결제 만료/취소)
                      │
                      ├─ waiting.status = WAITING → PROMOTED ✅
                      ├─ training_course_application.status = WAITING → ACCEPT
                      │
                      ▼
              자동 승격 완료 (ACCEPT)
                      │
                      ▼
              결제 기한 24시간
                      │
                      ▼
                 PAID (결제 완료)
```

### 🔍 대기열 테이블 구조 이해

시스템은 **2개의 테이블**을 사용하여 대기 상태를 관리합니다:

#### 1. `training_course_application` 테이블
```sql
-- 신청의 메인 상태 (가장 중요!)
application_id | status   | session_id | dog_id
---------------|----------|------------|--------
1              | WAITING  | 100        | 5
```

#### 2. `waiting` 테이블
```sql
-- 대기 상세 정보 (부가 정보)
application_id | status    | is_approved
---------------|-----------|-------------
1              | WAITING   | false
1              | PROMOTED  | true        ← 승격 후
```

#### 💡 왜 2개 테이블로 나눴을까?

1. **관심사 분리**
   - `training_course_application`: 모든 신청 상태 관리
   - `waiting`: 대기열만의 특수한 정보 (순번, 승격 이력)

2. **히스토리 추적**
   - `waiting.status = PROMOTED`: "이 사람은 대기에서 승격되었구나" 파악 가능
   - 통계: "오늘 몇 명이 대기에서 승격되었는지" 조회 가능

3. **`PROMOTED` 상태의 역할**
   - ✅ **기록용**: 대기 → 승격 이력 저장
   - ❌ **현재는 미사용**: 조회 로직 없음 (개선 여지 있음)

### 대기 순번 계산

```sql
-- 현재 사용자의 대기 순번
SELECT COUNT(*) + 1
FROM training_course_application
WHERE session_id = ?
  AND status = 'WAITING'
  AND created_at < 현재_사용자_신청시간
  AND is_deleted = 0
```

- 신청 시간이 빠를수록 순번이 높음 (FIFO 방식)

### 자동 승격 로직 (핵심!)

```java
private void promoteNextWaiting(Long sessionId) {
    // 1️⃣ 세션 정보 조회 (비관적 락)
    //   → SELECT FOR UPDATE로 동시성 제어
    TrainingSession session = dao.findSessionByIdForUpdate(sessionId);
    
    // 2️⃣ 현재 승인된 인원 확인
    int currentCount = dao.countApprovedApplications(sessionId);
    
    if (currentCount >= session.getMaxStudents()) {
        return; // 정원 가득 참
    }
    
    // 3️⃣ 가장 오래 대기한 사람 조회
    Long nextAppId = dao.findOldestWaitingApplicationId(sessionId);
    
    // 4️⃣ WAITING → ACCEPT 상태 변경
    dao.updateApplicationStatusSimple(nextAppId, "ACCEPT");
    dao.updateWaitingStatus(nextAppId, "PROMOTED");
    dao.updatePaymentDeadline(nextAppId, 24); // 24시간
    
    // 5️⃣ 출석 정보 생성
    createAttendanceRecord(nextAppId);
}
```

---

## 5. 기술 스택

### 스케줄링

| 기술 | 역할 | 설정 위치 |
|------|------|-----------|
| **Spring Scheduler** | 주기적 작업 실행 | `@EnableScheduling` |
| **Cron 표현식** | 실행 시간 설정 | `@Scheduled(cron = "...")` |
| **application.yml** | 설정 외부화 | `session.deadline.hours: 24` |

### 데이터 처리

| 기술 | 역할 | 사용 예시 |
|------|------|-----------|
| **MyBatis** | SQL 매핑 | XML 기반 쿼리 작성 |
| **Spring Transaction** | 트랜잭션 관리 | `@Transactional` |
| **MySQL** | 데이터 저장소 | 상태 정보 저장 |

### 동시성 제어

| 기술 | 역할 | 사용 위치 |
|------|------|-----------|
| **비관적 락 (Pessimistic Lock)** | 데이터 일관성 보장 | `SELECT FOR UPDATE` |
| **트랜잭션 격리** | 동시 처리 제어 | 대기자 승격 |

---

## 6. 핵심 기술 포인트

### 🔐 1. 동시성 제어 (SELECT FOR UPDATE)

#### 문제 상황
```
Thread A: 정원 확인 (4/5) → 승격 시도
Thread B: 정원 확인 (4/5) → 승격 시도
결과: 둘 다 승격 → 정원 초과 (6/5) ❌
```

#### 해결 방법
```sql
-- 비관적 락 적용
SELECT * FROM training_session
WHERE session_id = ?
FOR UPDATE;  -- 이 행을 잠금
```

```
Thread A: 락 획득 → 정원 확인 (4/5) → 승격 (5/5) → 락 해제
Thread B: 대기 → 락 획득 → 정원 확인 (5/5) → 승격 포기 ✅
```

### 🎯 2. Self-Invocation 문제 해결

#### 문제 상황
```java
@Component
class Scheduler {
    @Scheduled(cron = "...")
    public void schedule() {
        processLogic(); // @Transactional이 동작하지 않음!
    }
    
    @Transactional
    private void processLogic() {
        // 트랜잭션 적용 안됨
    }
}
```

#### 해결 방법
```java
// Scheduler (트랜잭션 없음)
@Component
class Scheduler {
    private final Service service;
    
    @Scheduled(cron = "...")
    public void schedule() {
        service.processLogic(); // 별도 Bean 호출
    }
}

// Service (트랜잭션 적용)
@Service
class Service {
    @Transactional
    public void processLogic() {
        // 트랜잭션 정상 동작!
    }
}
```

### 📦 3. 배치 처리 최적화

#### 일괄 업데이트 사용
```java
// ❌ 나쁜 예: N번 쿼리 실행
for (Long id : ids) {
    dao.updateStatus(id, "EXPIRED");
}

// ✅ 좋은 예: 1번 쿼리 실행
dao.updateStatusBatch(ids, "EXPIRED");
```

```xml
<!-- MyBatis XML -->
<update id="updateStatusBatch">
    UPDATE training_course_application
    SET status = #{status}
    WHERE application_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</update>
```

### 🛡️ 4. 트랜잭션 범위 설계

```java
@Transactional(rollbackFor = Exception.class)
public void processExpiredPayments() {
    // 1. 만료 처리
    dao.expireApplication(appId);
    
    // 2. 대기자 승격
    promoteNextWaiting(sessionId);
    
    // 3. 출석 정보 생성
    createAttendance(appId);
    
    // → 하나라도 실패하면 전체 롤백!
}
```

### ⚙️ 5. 설정 외부화

```yaml
# application.yml
session:
  deadline:
    enabled: true       # 수업 시작 마감 기능 활성화
    hours: 24           # 수업 시작 마감 시간

payment:
  deadline:
    enabled: true       # 결제 기한 만료 기능 활성화
    hours: 24           # 결제 기한

course:
  status:
    update:
      enabled: true     # 과정 상태 업데이트 활성화
```

```java
@Value("${session.deadline.hours:24}")
private int sessionDeadlineHours;  // 기본값 24시간

@Value("${session.deadline.enabled:true}")
private boolean enabled;  // 기본값 true
```

#### 💡 긴급 중단 시나리오

만약 스케줄러에 버그가 발견되면?

```yaml
# application.yml 수정 후 재시작
payment:
  deadline:
    enabled: false  # ← 이것만 변경!
```

→ 코드 수정 없이 즉시 기능 중단 가능! ✅

---

## 📊 시스템 흐름 요약

### 전체 프로세스 타임라인

```
시간     │ 사용자 액션        │ 시스템 자동 처리
─────────┼───────────────────┼────────────────────────────────
Day 1    │ 수업 신청         │ 
  00:00  │ (정원 초과)       │ → WAITING 상태 전환
         │                   │
Day 2    │                   │ SessionDeadlineScheduler 실행
  00:00  │                   │ → 수업 시작 24시간 전 마감
         │                   │ → 미승인 신청 EXPIRED 처리
         │                   │
Day 3    │ 다른 사용자 취소  │ PaymentDeadlineScheduler 실행
  10:03  │                   │ → 자리 발생
         │                   │ → 대기자 자동 승격 (ACCEPT)
         │                   │ → 결제 기한 24시간 설정
         │                   │
Day 4    │ 결제 완료         │ 
  08:00  │ (PAID)            │
         │                   │
Day 5    │                   │ CourseStatusScheduler 실행
  10:06  │                   │ → 첫 세션 시작
         │                   │ → 과정 상태: IN_PROGRESS
         │                   │
Day 30   │                   │ CourseStatusScheduler 실행
  18:06  │                   │ → 모든 세션 종료
         │                   │ → 과정 상태: DONE
```

---

## 🎓 학습 포인트 정리

### 1. Spring Scheduler
- `@EnableScheduling`: 스케줄링 기능 활성화
- `@Scheduled`: 주기적 작업 정의
- Cron 표현식: 복잡한 실행 시간 설정

### 2. 트랜잭션 관리
- `@Transactional`: 원자성 보장
- Self-invocation 문제와 해결
- 롤백 전략 (`rollbackFor = Exception.class`)

### 3. 동시성 제어
- 비관적 락 (SELECT FOR UPDATE)
- 데이터 일관성 보장
- 정원 초과 방지

### 4. MyBatis
- XML 기반 SQL 매핑
- 동적 쿼리 (`<foreach>`)
- 배치 업데이트 최적화

### 5. 시스템 설계
- 스케줄러 실행 시간 분산
- 설정 외부화 (application.yml)
- 긴급 중단 플래그 설계

---

## 💡 추가 개선 아이디어

### 1. 알림 시스템 연동
- 대기자 승격 시 FCM 푸시 알림
- 결제 기한 임박 알림 (D-1)

### 2. 모니터링
- 스케줄러 실행 로그 대시보드
- 처리 건수 통계
- 오류 알림 (Slack, 이메일)

### 3. 성능 최적화
- 인덱스 최적화 (`session_id`, `status`, `created_at`)
- 쿼리 실행 계획 분석
- 배치 크기 조정

---

## 📝 참고 자료

- [Spring Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [MyBatis Dynamic SQL](https://mybatis.org/mybatis-3/dynamic-sql.html)
- [MySQL Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)

---

**작성일**: 2026-01-16  
**작성자**: GitHub Copilot  
**버전**: 1.0

