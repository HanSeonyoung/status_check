# 1단계: Gradle 빌드용 이미지
FROM gradle:8.5.0-jdk21 AS builder
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build -x test

# 2단계: 실제 앱 실행 이미지
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]

