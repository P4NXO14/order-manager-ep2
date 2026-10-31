FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean verify

FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build --chown=app:app /app/target/order-manager-1.0.0.jar app.jar

USER app

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]