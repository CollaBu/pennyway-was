FROM openjdk:17
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar

ARG PROFILE=dev
ENV PROFILE=${PROFILE}

ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=${PROFILE}","-Duser.timezone=Asia/Seoul"]