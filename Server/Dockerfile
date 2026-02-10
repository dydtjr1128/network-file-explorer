# 자바 jdk 11 환경
FROM eclipse-temurin:11-jdk-alpine

# 리눅스의 /app을 저장공간으로 사용하겠다는 의미. cd backend 동일
WORKDIR /backend

VOLUME /tmp

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]