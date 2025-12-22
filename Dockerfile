
## --------------------------
## 1단계: 빌드
## --------------------------
FROM gradle:7.6-jdk17 AS builder

WORKDIR /app

# 소스 복사 및 권한 부여
COPY --chown=gradle:gradle . .

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle 빌드 (fat jar 생성)
RUN ./gradlew clean build -x test --no-daemon

# --------------------------
# 2단계: 런타임
# --------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 단계에서 생성된 jar만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션 + 실행
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]