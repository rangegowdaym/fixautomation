# FIX Automation

An end-to-end test automation framework for **FIX order processing** workflows. It validates order lifecycle events by sending orders over **WebSocket** and verifying responses through the **RabbitMQ Management API**, all driven by **Cucumber BDD** scenarios running on **TestNG** with **Allure** reporting.

---

## Tech Stack

| Layer              | Technology                                     |
| ------------------ | ---------------------------------------------- |
| Language           | Java 21                                        |
| Framework          | Spring Boot 3.3.x                              |
| BDD                | Cucumber 7.15 + TestNG 7.9                     |
| WebSocket Client   | OkHttp 4.12                                    |
| RabbitMQ API       | REST Assured 6.0 (Management HTTP API)         |
| Polling / Retry    | Awaitility 4.2                                 |
| Reporting          | Allure 2.25 (Cucumber 7 JVM + TestNG adapters) |
| Build              | Gradle 8.x (Groovy DSL)                        |
| Serialization      | Jackson                                        |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/fixautomation/
│   │   ├── FixAutomationApplication.java      # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── AppConfig.java                 # Externalized configuration (WS, RabbitMQ, timeouts)
│   │   │   └── OkHttpConfig.java              # OkHttp client bean
│   │   ├── context/
│   │   │   └── TestContext.java               # Cucumber-glue scoped context (thread-safe)
│   │   ├── model/
│   │   │   ├── OrderRequest.java              # Order payload sent via WebSocket
│   │   │   ├── AckResponse.java               # ACK received from WebSocket
│   │   │   └── OrderResponse.java             # Final order response from RabbitMQ
│   │   ├── rabbitmq/
│   │   │   └── RabbitMQManagementService.java # Publish / get messages via REST Assured
│   │   ├── utils/
│   │   │   ├── JsonValidator.java             # JSON assertion helpers
│   │   │   ├── LoggingInterceptor.java        # OkHttp logging interceptor
│   │   │   └── RetryUtils.java               # Awaitility-based polling utility
│   │   └── websocket/
│   │       └── WebSocketService.java          # Send orders & wait for ACK over WebSocket
│   └── resources/
│       ├── application.properties             # Default profile (dev)
│       ├── application-dev.properties         # Dev environment config
│       ├── application-uat.properties         # UAT environment config
│       └── application-prod.properties        # Production environment config
└── test/
    ├── java/com/fixautomation/
    │   ├── config/
    │   │   └── CucumberSpringConfiguration.java  # @CucumberContextConfiguration + @SpringBootTest
    │   ├── hooks/
    │   │   └── CucumberHooks.java                # Before/After hooks with Allure integration
    │   ├── runner/
    │   │   └── TestNGRunner.java                 # Cucumber-TestNG runner (parallel scenarios)
    │   └── steps/
    │       └── OrderStepDefinitions.java         # Step definitions for order scenarios
    └── resources/
        ├── testng.xml                             # TestNG suite (parallel methods, 4 threads)
        └── features/
            └── order.feature                      # Cucumber feature file
```

---

## Prerequisites

- **Java 21** (or later)
- A running **WebSocket server** that accepts FIX order requests at the configured endpoint
- A running **RabbitMQ** instance with the Management Plugin enabled (port `15672`)

---

## Configuration

Environment-specific settings are managed through Spring profiles in `src/main/resources/`:

| Property                          | Description                           | Default (dev)                    |
| --------------------------------- | ------------------------------------- | -------------------------------- |
| `websocket.url`                   | WebSocket endpoint for order requests | `ws://localhost:8080/ws/orders`  |
| `rabbitmq.management.host`        | RabbitMQ Management API base URL      | `http://localhost:15672`         |
| `rabbitmq.management.username`    | RabbitMQ username                     | `guest`                          |
| `rabbitmq.management.password`    | RabbitMQ password                     | `guest`                          |
| `rabbitmq.management.vhost`       | RabbitMQ virtual host                 | `/`                              |
| `rabbitmq.request.queue`          | Queue for order requests              | `order-request-queue`            |
| `rabbitmq.response.queue`         | Queue for order responses             | `order-response-queue`           |
| `polling.timeout.seconds`         | Max wait time when polling RabbitMQ   | `30`                             |
| `polling.interval.millis`         | Poll interval in milliseconds         | `500`                            |
| `websocket.ack.timeout.seconds`   | Timeout for WebSocket ACK             | `10`                             |

Available profiles: **`dev`** (default), **`uat`**, **`prod`**.

---

## How to Build

```bash
./gradlew clean build
```

---

## Running Tests

### Default (dev) environment

```bash
./gradlew test
```

### Specific environment

```bash
./gradlew test -Denv=uat
./gradlew test -Denv=prod
```

### Run by Cucumber tag

```bash
./gradlew test -Dcucumber.filter.tags="@smoke"
```

---

## Test Workflow

```
┌──────────────┐    WebSocket     ┌──────────────────┐
│  Test Client  │ ───────────────▶│  Order Service    │
│  (OkHttp)     │ ◀─── ACK ──────│  (WS Endpoint)    │
└──────────────┘                  └────────┬─────────┘
       │                                   │
       │  REST Assured                     │  Publishes response
       │  (Poll / Get message)             ▼
       │                          ┌──────────────────┐
       └─────────────────────────▶│    RabbitMQ       │
                                  │  (Mgmt HTTP API)  │
                                  └──────────────────┘
```

1. **Send order** — `WebSocketService` opens a connection, sends the JSON-serialized `OrderRequest`, and waits for an ACK.
2. **Verify ACK** — The ACK is parsed into `AckResponse` and assertions are made.
3. **Poll RabbitMQ** — `RabbitMQManagementService` polls the response queue via REST Assured until the final `OrderResponse` arrives (using `RetryUtils` / Awaitility).
4. **Assert response** — Step definitions verify status, execution type, and other fields.

---

## Parallel Execution

Tests run in parallel at the scenario level:

- **TestNG** suite is configured with `parallel="methods"` and `thread-count="4"` (`testng.xml`).
- **Cucumber** `@DataProvider(parallel = true)` is enabled in `TestNGRunner`.
- **TestContext** uses `@Scope("cucumber-glue")` to ensure each scenario gets its own isolated state.

---

## Allure Reporting

Test results are written to `build/allure-results`. To generate and view the report:

```bash
# Generate the report
allure generate build/allure-results -o build/allure-report --clean

# Open in browser
allure open build/allure-report
```

> **Note:** Requires the [Allure CLI](https://docs.qameta.io/allure/#_installing_a_commandline) to be installed.

---

## Cucumber Reports

An HTML report is also generated at:

```
build/cucumber-reports/cucumber.html
```

---

## Key Design Decisions

| Decision | Rationale |
| --- | --- |
| **REST Assured for RabbitMQ** | Uses the Management HTTP API instead of an AMQP client — no broker-level dependency in test code |
| **OkHttp for WebSocket** | Lightweight, well-supported WebSocket client that integrates easily with Spring |
| **Awaitility for polling** | Declarative, timeout-aware polling that avoids manual `Thread.sleep` loops |
| **Spring `@Scope("cucumber-glue")`** | Provides per-scenario isolation for safe parallel execution |
| **Profile-based config** | Switch between dev / uat / prod without code changes |

---

## License

_Internal / proprietary — see your organization's licensing policy._

