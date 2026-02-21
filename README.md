# Micro-savings Retirement API

A containerized Java + Spring Boot API for micro-savings retirement calculations.
Parses expenses into transactions, validates and filters by temporal periods (q/p/k),
and computes NPS & index fund returns with inflation adjustment and tax-benefit reporting.

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for container deployment)

## Build

```bash
mvn clean package -DskipTests
```

## Run locally

```bash
java -jar target/micro-savings-1.0.0.jar
```

The API starts on **port 5477**.

## Run with Docker

```bash
docker build -t blk-hacking-ind-ponshankar-balasubramaniyam .
docker run -p 5477:5477 blk-hacking-ind-ponshankar-balasubramaniyam
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/blackrock/challenge/v1/transactions:parse` | Parse expenses into transactions |
| POST | `/blackrock/challenge/v1/transactions:validator` | Validate transactions |
| POST | `/blackrock/challenge/v1/transactions:filter` | Filter transactions by q/p/k periods |
| POST | `/blackrock/challenge/v1/returns:nps` | Compute NPS returns |
| POST | `/blackrock/challenge/v1/returns:index` | Compute index fund returns |
| GET | `/blackrock/challenge/v1/performance` | JVM performance metrics |

## Run tests

```bash
mvn test
```

## Project Structure

```
src/main/java/com/blackrock/hackathon/
├── MicroSavingsApplication.java     # Spring Boot entry point
├── config/                          # Web config, interceptor, exception handler
├── controller/                      # REST endpoint handlers
├── model/                           # Domain models (Expense, Transaction, Period, etc.)
│   ├── request/                     # Request DTOs
│   └── response/                    # Response DTOs
├── service/                         # Business logic (parse, validate, filter, returns, perf)
└── util/                            # Time parsing, tax calculation helpers
```
