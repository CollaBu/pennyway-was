# Common 모듈 빌드
FROM openjdk:17 AS common-builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY pennyway-common pennyway-common
RUN chmod +x ./gradlew


COPY pennyway-common pennyway-common
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew :pennyway-common:build -x test

# Infra 모듈 빌드
FROM openjdk:17 AS infra-builder
WORKDIR /app
COPY --from=common-builder /app/pennyway-common/build/libs/*.jar lib/
COPY pennyway-infra pennyway-infra
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew :pennyway-infra:build -x test

# Domain 모듈 빌드
FROM openjdk:17 AS domain-builder
WORKDIR /app
COPY --from=common-builder /app/pennyway-common/build/libs/*.jar lib/
COPY --from=infra-builder /app/pennyway-infra/build/libs/*.jar lib/
COPY pennyway-domain pennyway-domain
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew :pennyway-domain:build -x test

# 최종 실행 이미지
FROM openjdk:17

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=common-builder /app/pennyway-common/build/libs/*.jar common.jar
COPY --from=infra-builder /app/pennyway-infra/build/libs/*.jar infra.jar
COPY --from=domain-builder /app/pennyway-domain/build/libs/*.jar domain.jar

# 클래스패스 설정
ENV CLASSPATH=/app/common.jar:/app/infra.jar:/app/domain.jar