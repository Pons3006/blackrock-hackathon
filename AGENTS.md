# AGENTS.md

## Cursor Cloud specific instructions

### Overview

This is a **Micro-savings Retirement API** — a stateless Java 21 + Spring Boot 3.4.3 REST API. No databases, caches, or message queues are required.

### Prerequisites

- **Java 21** (pre-installed as `java-21-openjdk-amd64`)
- **Maven** (installed via `apt-get install -y maven`)

### Key commands

See `README.md` for full details. Quick reference:

| Task | Command |
|------|---------|
| Build | `mvn clean package -DskipTests` |
| Test (all) | `mvn test` |
| Test (stress) | `mvn test -Pstress` |
| Run | `java -jar target/micro-savings-1.0.0.jar` |

### Non-obvious notes

- The app runs on **port 5477** (not the Spring Boot default 8080).
- Swagger UI is served at the root path `/`, so `http://localhost:5477` opens Swagger docs directly.
- OpenTelemetry tracing is **disabled by default** (`OTEL_TRACING_ENABLED=false`). Jaeger is only needed for stress-test trace visualization and is not required for development.
- Maven 3.8.7 (from Ubuntu apt) works fine despite the README stating 3.9+.
- `JAVA_HOME` should be set to `/usr/lib/jvm/java-21-openjdk-amd64` if not already configured.
- The stress test profile (`-Pstress`) is separate from the default test suite and requires the app to be running since it makes HTTP calls.
- There are 100 unit/integration tests in the default suite; all pass without any external services.
