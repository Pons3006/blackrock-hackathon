# Build: docker build -t blk-hacking-ind-ponshankar-balasubramaniyam .
# Run:   docker run -p 5477:5477 blk-hacking-ind-ponshankar-balasubramaniyam

# ── Stage 1: Build ──────────────────────────────────────────────────
# Alpine Linux — minimal footprint for faster CI builds; JDK layer discarded after compile.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven \
    && mvn clean package -DskipTests -q

# ── Stage 2: Runtime ────────────────────────────────────────────────
# Alpine Linux chosen for minimal image size (~180 MB vs ~400 MB with Debian),
# fast pull/deploy times, and reduced attack surface while still providing
# full glibc compatibility via Eclipse Temurin.
FROM eclipse-temurin:21-jre-alpine AS runtime  # OS: Alpine Linux (see criteria above)

WORKDIR /app

COPY --from=build /build/target/micro-savings-1.0.0.jar app.jar

EXPOSE 5477

ENV OTEL_TRACING_ENABLED=false
ENV OTEL_EXPORTER_ENDPOINT=http://localhost:4318/v1/traces
ENV OTEL_SAMPLING_PROBABILITY=1.0

ENTRYPOINT ["java", "-jar", "app.jar"]
