# Micro-savings Retirement API

A production-grade Java 21 + Spring Boot API that enables automated retirement savings
through expense-based micro-investments. The system rounds up each expense to the
nearest multiple of 100, applies temporal constraint rules (q/p/k periods), calculates
compound-interest returns for NPS and Index Fund instruments, and adjusts for inflation.

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for container deployment)

## Build

```bash
mvn clean package -DskipTests
```

## Run Locally

```bash
java -jar target/micro-savings-1.0.0.jar
```

The API starts on **port 5477**. Swagger UI is available at [http://localhost:5477](http://localhost:5477).

## Run with Docker

```bash
docker build -t blk-hacking-ind-ponshankar-balasubramaniyam .
docker run -d -p 5477:5477 blk-hacking-ind-ponshankar-balasubramaniyam
```

The Docker image uses a multi-stage build with `eclipse-temurin:21-jre-alpine` as the
runtime base. Alpine was chosen for minimal image size (~180 MB vs ~400 MB Debian) and
reduced attack surface.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/blackrock/challenge/v1/transactions:parse` | Parse expenses into transactions |
| POST | `/blackrock/challenge/v1/transactions:validator` | Validate transactions |
| POST | `/blackrock/challenge/v1/transactions:filter` | Filter transactions by q/p/k periods |
| POST | `/blackrock/challenge/v1/returns:nps` | Compute NPS returns (7.11% annual, tax benefit) |
| POST | `/blackrock/challenge/v1/returns:index` | Compute Index Fund returns (14.49% annual) |
| GET  | `/blackrock/challenge/v1/performance` | JVM performance metrics |

### 1. Parse Expenses

Rounds each expense amount up to the next multiple of 100 and computes the remanent
(the difference used for micro-investment).

```bash
curl -s -X POST http://localhost:5477/blackrock/challenge/v1/transactions:parse \
  -H 'Content-Type: application/json' \
  -d '{
    "expenses": [
      { "timestamp": "2023-10-12 20:15:00", "amount": 250 },
      { "timestamp": "2023-02-28 15:49:00", "amount": 375 },
      { "timestamp": "2023-07-01 21:59:00", "amount": 620 },
      { "timestamp": "2023-12-17 08:09:00", "amount": 480 }
    ]
  }'
```

**Response** (abbreviated):

```json
{
  "transactions": [
    { "date": "2023-10-12 20:15:00", "amount": 250.0, "ceiling": 300.0, "remanent": 50.0 },
    { "date": "2023-02-28 15:49:00", "amount": 375.0, "ceiling": 400.0, "remanent": 25.0 },
    { "date": "2023-07-01 21:59:00", "amount": 620.0, "ceiling": 700.0, "remanent": 80.0 },
    { "date": "2023-12-17 08:09:00", "amount": 480.0, "ceiling": 500.0, "remanent": 20.0 }
  ],
  "totals": { "amountSum": 1725.0, "ceilingSum": 1900.0, "remanentSum": 175.0 }
}
```

### 2. Validate Transactions

Checks amount bounds (`0 <= amount < 500,000`), ceiling correctness (multiple of 100,
`ceiling >= amount`, `ceiling - amount < 100`), remanent consistency, and duplicate
detection by timestamp.

```bash
curl -s -X POST http://localhost:5477/blackrock/challenge/v1/transactions:validator \
  -H 'Content-Type: application/json' \
  -d '{
    "wage": 50000,
    "transactions": [
      { "date": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 },
      { "date": "2023-10-12 20:15:00", "amount": 375, "ceiling": 400, "remanent": 25 },
      { "date": "2023-07-01 21:59:00", "amount": -5, "ceiling": 0, "remanent": 5 }
    ]
  }'
```

**Response**: categorises into `valid`, `invalid` (with `message`), and `duplicate` lists.

### 3. Filter by Temporal Constraints

Validates q/p/k period definitions and filters transactions based on k-period coverage.
Transactions not falling within any k-period range are returned as invalid.

```bash
curl -s -X POST http://localhost:5477/blackrock/challenge/v1/transactions:filter \
  -H 'Content-Type: application/json' \
  -d '{
    "q": [{ "fixed": 0, "start": "2023-07-01 00:00:00", "end": "2023-07-31 23:59:00" }],
    "p": [{ "extra": 25, "start": "2023-10-01 08:00:00", "end": "2023-12-31 19:59:00" }],
    "k": [
      { "start": "2023-03-01 00:00:00", "end": "2023-11-30 23:59:00" },
      { "start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:00" }
    ],
    "transactions": [
      { "date": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 },
      { "date": "2023-02-28 15:49:00", "amount": 375, "ceiling": 400, "remanent": 25 },
      { "date": "2023-07-01 21:59:00", "amount": 620, "ceiling": 700, "remanent": 80 },
      { "date": "2023-12-17 08:09:00", "amount": 480, "ceiling": 500, "remanent": 20 }
    ]
  }'
```

### 4. Compute NPS Returns

Calculates National Pension Scheme returns at 7.11% annual compound interest with
inflation adjustment and NPS tax benefit computation.

```bash
curl -s -X POST http://localhost:5477/blackrock/challenge/v1/returns:nps \
  -H 'Content-Type: application/json' \
  -d '{
    "age": 29,
    "wage": 50000,
    "inflation": 0.055,
    "q": [{ "fixed": 0, "start": "2023-07-01 00:00:00", "end": "2023-07-31 23:59:00" }],
    "p": [{ "extra": 25, "start": "2023-10-01 08:00:00", "end": "2023-12-31 19:59:00" }],
    "k": [
      { "start": "2023-03-01 00:00:00", "end": "2023-11-30 23:59:00" },
      { "start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:00" }
    ],
    "transactions": [
      { "date": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 },
      { "date": "2023-02-28 15:49:00", "amount": 375, "ceiling": 400, "remanent": 25 },
      { "date": "2023-07-01 21:59:00", "amount": 620, "ceiling": 700, "remanent": 80 },
      { "date": "2023-12-17 08:09:00", "amount": 480, "ceiling": 500, "remanent": 20 }
    ]
  }'
```

### 5. Compute Index Fund Returns

Same structure as NPS, but at 14.49% annual compound interest with no tax benefit.

```bash
curl -s -X POST http://localhost:5477/blackrock/challenge/v1/returns:index \
  -H 'Content-Type: application/json' \
  -d '{
    "age": 29,
    "wage": 50000,
    "inflation": 0.055,
    "q": [], "p": [],
    "k": [{ "start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:00" }],
    "transactions": [
      { "date": "2023-06-15 10:00:00", "amount": 250, "ceiling": 300, "remanent": 50 }
    ]
  }'
```

### 6. Performance Metrics

```bash
curl -s http://localhost:5477/blackrock/challenge/v1/performance
```

**Response**:

```json
{ "time": "00:05:23.417", "memory": "128.45 MB", "threads": 22 }
```

## Algorithm Design

### Transaction Parsing — O(n)

Each expense is rounded up: `ceiling = ceil(amount / 100) * 100`, `remanent = ceiling - amount`.
Single pass over all expenses with running totals.

### Period Processing — Sweep-line with Priority Queues

Transactions are sorted by date (epoch seconds) once. Period rules are then applied
in a single sweep:

- **q-period overrides** — O((n + q) log q): A max-heap keyed by start date processes
  q-periods as a sweep line. For each transaction, all q-periods that have started are
  pushed into the heap, expired ones are removed, and the top of the heap (latest start,
  ties broken by list order) provides the fixed override.

- **p-period extras** — O((n + p) log p): A min-heap keyed by end date tracks active
  p-periods. A running sum of active extras is maintained; as periods enter/exit the
  sweep window, the sum is updated and added to each transaction's remanent.

- **k-period grouping** — O(n + k log n): A prefix-sum array over the sorted remanents
  enables O(log n) range-sum queries per k-period via binary search (lower/upper bound).

### Returns Calculation

- **Compound interest**: `A = P * (1 + r)^t` where r is 7.11% (NPS) or 14.49% (Index),
  and t = max(60 - age, 5) years.
- **Inflation adjustment**: `A_real = A_nominal / (1 + inflation)^t`
- **NPS tax benefit**: `deduction = min(invested, 10% * annual_income, ₹2,00,000)`,
  `benefit = Tax(income) - Tax(income - deduction)` using simplified Indian tax slabs.

### Tax Slabs (Simplified)

| Slab | Rate |
|------|------|
| ₹0 – ₹7,00,000 | 0% |
| ₹7,00,001 – ₹10,00,000 | 10% |
| ₹10,00,001 – ₹12,00,000 | 15% |
| ₹12,00,001 – ₹15,00,000 | 20% |
| Above ₹15,00,000 | 30% |

## Testing

### Run All Tests

```bash
mvn test
```

### Run by Category

```bash
# Unit tests only (services + utilities)
mvn test -Dtest="TransactionParseServiceTest,TransactionValidatorServiceTest,TransactionFilterServiceTest,ReturnsServiceTest,TaxUtilsTest,PeriodEngineTest,TimeUtilsTest"

# Integration tests only (full HTTP endpoint tests)
mvn test -Dtest="EndpointIntegrationTest"

# Single test class
mvn test -Dtest=ReturnsServiceTest
```

### Test Coverage

| Category | Test Class | What It Validates |
|----------|-----------|-------------------|
| Unit | `TransactionParseServiceTest` | Ceiling/remanent calculation, totals aggregation |
| Unit | `TransactionValidatorServiceTest` | Amount bounds, ceiling rules, duplicate detection |
| Unit | `TransactionFilterServiceTest` | k-period coverage filtering, period validation |
| Unit | `ReturnsServiceTest` | NPS/Index compounding, inflation, tax benefits, q/p overrides |
| Unit | `TaxUtilsTest` | Tax slab calculation, NPS deduction caps |
| Unit | `PeriodEngineTest` | Sorting, q/p application, k-period grouping |
| Unit | `TimeUtilsTest` | Epoch conversion, format parsing, error handling |
| Integration | `EndpointIntegrationTest` | Full REST endpoint round-trips (9 ordered tests) |
| Integration | `MicroSavingsApplicationTests` | Spring context loading |

Test files are located under `src/test/java/` and include metadata comments specifying
test type, validation scope, and execution command as required by the challenge.

## Project Structure

```
src/main/java/com/ponshankar/hackathon/blackrock/
├── MicroSavingsApplication.java        # Spring Boot entry point
├── config/
│   ├── GlobalExceptionHandler.java     # Maps exceptions to HTTP 400/501
│   ├── WebConfig.java                  # Registers latency interceptor
│   ├── RequestLatencyInterceptor.java  # Lock-free request metrics tracking
│   ├── ApiHealthIndicator.java         # Custom health check (heap, latency thresholds)
│   └── ApplicationInfoContributor.java # Runtime stats for /actuator/info
├── controller/
│   ├── TransactionController.java      # parse, validator, filter endpoints
│   ├── ReturnsController.java          # NPS and Index return endpoints
│   └── PerformanceController.java      # JVM metrics endpoint
├── model/
│   ├── Expense.java                    # Input: timestamp + amount
│   ├── Transaction.java                # Enriched: date + amount + ceiling + remanent
│   ├── Period.java                     # q/p/k period definition
│   ├── Totals.java                     # Aggregate sums
│   ├── SavingsByDate.java              # Per-k-period return breakdown
│   ├── request/                        # ParseRequest, ValidatorRequest, FilterRequest, ReturnsRequest
│   └── response/                       # ParseResponse, ValidatorResponse, FilterResponse, ReturnsResponse, PerformanceResponse
├── service/
│   ├── TransactionParseService.java    # Ceiling rounding and remanent calculation
│   ├── TransactionValidatorService.java# Input validation and duplicate detection
│   ├── TransactionFilterService.java   # k-period coverage filtering
│   ├── ReturnsService.java             # Compound interest, inflation, tax benefit
│   └── PerformanceService.java         # JVM uptime, heap, thread count
└── util/
    ├── TimeUtils.java                  # Timestamp parsing and epoch conversion
    ├── PeriodEngine.java               # Sweep-line q/p processing, prefix-sum k grouping
    └── TaxUtils.java                   # Indian tax slab computation
```

## Additional Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Swagger UI (interactive API docs) |
| GET | `/actuator/health` | Health check with heap usage and latency thresholds |
| GET | `/actuator/info` | Application metadata and runtime statistics |
