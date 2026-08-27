# ── Stage 1: Build ────────────────────────────────────────────────
# Maven + JDK 17 to compile and package the app into a JAR.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy dependency manifest first — better layer caching.
# Maven re-downloads dependencies only when pom.xml changes.
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Copy source and build the JAR.
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────
# JRE only — no JDK, smaller final image.
# eclipse-temurin:17-jre supports arm64 (Apple Silicon M1/M2/M3/M5 Macs).
# Do NOT use eclipse-temurin:17-jre-alpine — it has no arm64 build.
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy only the built JAR from the build stage.
COPY --from=build /app/target/devops-demo-app-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
