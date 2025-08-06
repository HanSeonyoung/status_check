# 1단계: Gradle 빌드용 이미지
FROM gradle:8.5.0-jdk21 AS builder
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle build -x test

# 2단계: 앱 실행 이미지
FROM eclipse-temurin:21-jre

# 필요한 도구 설치 (curl, tar 등)
RUN apt-get update && apt-get install -y curl xz-utils && rm -rf /var/lib/apt/lists/*
RUN apt-get update && apt-get install -y iputils-ping

# 최신 ffmpeg 정적 빌드 다운로드 및 설치
RUN curl -L -o /tmp/ffmpeg.tar.xz https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz && \
    mkdir -p /opt/ffmpeg && \
    tar -xf /tmp/ffmpeg.tar.xz -C /opt/ffmpeg --strip-components=1 && \
    mv /opt/ffmpeg/ffprobe /usr/local/bin/ffprobe && \
    chmod +x /usr/local/bin/ffprobe && \
    rm -rf /tmp/ffmpeg.tar.xz

# 앱 실행 설정
WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
