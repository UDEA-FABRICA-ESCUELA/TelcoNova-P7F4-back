FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn dependency:resolve

COPY src src
RUN mvn package -DskipTests


FROM eclipse-temurin:21-jre-alpine
EXPOSE 8080
ARG JAR_FILE=target/supportsuite-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]