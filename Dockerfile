# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY web/pom.xml web/
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY web/src web/src
# Tests run in `make test` / CI; the image build only packages.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl web -am package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
COPY --from=build /workspace/web/target/events-app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
