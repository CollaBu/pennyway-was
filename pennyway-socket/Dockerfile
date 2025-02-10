FROM openjdk:17
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar

ARG PROFILE=dev
ENV PROFILE=${PROFILE}

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=${PROFILE}","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Seoul"]