# ------------------------------------------------------------------------------
# Build Stage: Compile and package Compilr Server using OpenJDK 25
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and POM first to leverage layer caching
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Pre-fetch Maven dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Runtime Stage: Minimal production OpenJDK 25 image
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Install docker CLI to allow diagnostic execution if needed
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy compiled jar from builder stage
COPY --from=builder /build/target/compilr-*.jar app.jar

ENV SERVER_PORT=8080 \
    COMPILR_EXECUTION_DOCKER_HOST=unix:///var/run/docker.sock

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/compiler/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
