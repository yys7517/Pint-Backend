FROM gradle:8.14.4-jdk17 AS builder
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/upload && chown -R spring:spring /app

USER spring
EXPOSE 8080

ENV APP_UPLOAD_DIR=/app/upload

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
