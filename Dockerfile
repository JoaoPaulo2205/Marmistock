FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system marmistock \
    && useradd --system --gid marmistock --home-dir /app marmistock

COPY --from=build --chown=marmistock:marmistock /workspace/target/*.jar app.jar

USER marmistock

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
