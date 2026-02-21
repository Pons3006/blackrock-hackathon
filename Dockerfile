# Build: docker build -t blk-hacking-ind-ponshankar-balasubramaniyam .
# Run:   docker run -p 5477:5477 blk-hacking-ind-ponshankar-balasubramaniyam

# ── Stage 1: Build ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven \
    && mvn clean package -DskipTests -q

# ── Stage 2: Runtime ────────────────────────────────────────────────
# Alpine chosen for minimal image size (~180 MB vs ~400 MB with Debian),
# fast pull/deploy times, and reduced attack surface while still
# providing full glibc compatibility via Eclipse Temurin.
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /build/target/micro-savings-1.0.0.jar app.jar

EXPOSE 5477

ENTRYPOINT ["java", "-jar", "app.jar"]
