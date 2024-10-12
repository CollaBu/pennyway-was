## Stage 1: Build common module
#FROM openjdk:17 AS common-builder
#WORKDIR /app
#COPY pennyway-common ./pennyway-common
#COPY build.gradle settings.gradle ./
#RUN ./gradlew :pennyway-common:build
#
## Stage 2: Build infra module
#FROM common-builder AS infra-builder
#COPY pennyway-infra ./pennyway-infra
#RUN ./gradlew :pennyway-infra:build
#
## Stage 3: Build domain module
#FROM infra-builder AS domain-builder
#COPY pennyway-domain ./pennyway-domain
#RUN ./gradlew :pennyway-domain:build
#
## Stage 4: Build and package the application (e.g., external-api)
#FROM domain-builder AS app-builder
#ARG APP_MODULE=pennyway-app-external-api
#COPY ${APP_MODULE} ./${APP_MODULE}
#RUN ./gradlew :${APP_MODULE}:build
#
## Final stage: Setup the runtime environment
#FROM openjdk:17-slim
#WORKDIR /app
#ARG APP_MODULE=pennyway-app-external-api
#COPY --from=app-builder /app/${APP_MODULE}/build/libs/*.jar ./app.jar
#
#ARG PROFILE=dev
#ENV PROFILE=${PROFILE}
#
#ENTRYPOINT ["java", "-cp", "${CLASSPATH}:/app/app.jar", "kr.co.pennyway.PennywayExternalApiApplication","/app.jar","--spring.profiles.active=${PROFILE}","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Seoul"]

FROM openjdk:17
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar

ARG PROFILE=dev
ENV PROFILE=${PROFILE}

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=${PROFILE}","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Seoul"]