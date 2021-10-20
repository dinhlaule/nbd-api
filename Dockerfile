FROM maven:3.8.1-jdk-8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} nbd-api.jar
ENTRYPOINT ["java","-jar","/nbd-api.jar"]