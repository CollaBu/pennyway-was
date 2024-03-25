FROM openjdk:17
ARG JAR_FILE=pennyway-app-external-api/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=dev","-Duser.timezone=Asia/Seoul"]