FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src src

RUN chmod +x gradlew && \
    JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew shadowJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 9090

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
