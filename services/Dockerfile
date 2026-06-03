# Shared multi-stage build for all Spring Boot microservices.
# The target module is selected with the MODULE build arg.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy POMs first to leverage Docker layer caching for dependencies.
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY property-service/pom.xml property-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY schedule-service/pom.xml schedule-service/pom.xml
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -q -B dependency:go-offline || true

COPY . .
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
