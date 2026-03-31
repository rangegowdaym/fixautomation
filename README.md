# FIX Automation Framework

A production-ready, end-to-end test automation framework for **FIX Engine Inbound** scenario testing. It connects to a FIX Engine as an **initiator** using **QuickFIX/J**, validates **ExecutionReport** responses, and cross-validates trade events over **RabbitMQ** (Management API) and **WebSocket** streams. All scenarios are driven by **Cucumber BDD** running on **TestNG** with **Allure** reporting.

---

## Tech Stack

| Layer                | Technology                                         |
|----------------------|----------------------------------------------------|
| Language             | Java 17                                            |
| Build Tool           | Gradle 8+                                          |
| Framework            | Spring Boot 3.3.x                                  |
| FIX Library          | QuickFIX/J 2.3.1 (FIX 4.4)                        |
| BDD                  | Cucumber 7.15 + TestNG 7.9                         |
| WebSocket Client     | OkHttp 4.12                                        |
| RabbitMQ API         | REST Assured 6.0 (Management HTTP API)             |
| Polling / Retry      | Awaitility 4.2                                     |
| Reporting            | Allure 2.25 (Cucumber 7 JVM + TestNG adapters)     |
| Logging              | Log4j2 2.23.1                                      |
| Config               | YAML (Spring Boot + SnakeYAML)                     |
| Dependency Injection | Spring Boot                                        |
| Serialization        | Jackson (JSON + YAML)                              |

---

## Project Architecture

```
src/
├── main/
│   ├── java/com/fixautomation/
│   │   ├── FixAutomationApplication.java          # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── AppConfig.java                     # WebSocket / RabbitMQ config
│   │   │   ├── FixConfig.java                     # FIX engine config (@ConfigurationProperties)
│   │   │   └── OkHttpConfig.java                  # OkHttp client bean
│   │   ├── context/
│   │   │   └── TestContext.java                   # Cucumber-glue scoped context (thread-safe)
│   │   ├── fixclient/
│   │   │   ├── FixApplication.java                # QuickFIX/J Application impl (lifecycle events)
│   │   │   ├── FixSessionManager.java             # Initiator lifecycle, send API
│   │   │   ├── FixMessageListener.java            # Routes inbound FIX messages by type
│   │   │   └── FixResponseStore.java              # Thread-safe ConcurrentHashMap response store
│   │   ├── fixbuilder/
│   │   │   └── FixMessageBuilder.java             # Fluent builder (NOS, Cancel, Replace, Status)
│   │   ├── models/
│   │   │   ├── FixOrderModel.java                 # Domain model for FIX order
│   │   │   └── ExecutionReportModel.java          # Domain model for ExecutionReport
│   │   ├── model/
│   │   │   ├── OrderRequest.java                  # WebSocket order payload
│   │   │   ├── AckResponse.java                   # WebSocket ACK response
│   │   │   └── OrderResponse.java                 # RabbitMQ final response
│   │   ├── rabbitmq/
│   │   │   └── RabbitMQManagementService.java     # Publish / consume via REST Assured
│   │   ├── validators/
│   │   │   ├── FixTagValidator.java               # Low-level FIX tag assertions
│   │   │   ├── OrderStatusValidator.java          # OrdStatus, ClOrdID, Symbol, OrderID
│   │   │   └── ExecutionReportValidator.java      # ExecType, Qty, AvgPx, full report checks
│   │   ├── utils/
│   │   │   ├── FixMessageLogger.java              # Structured FIX message logging
│   │   │   ├── JsonValidator.java                 # JSON field assertions
│   │   │   ├── LoggingInterceptor.java            # OkHttp HTTP logging
│   │   │   └── RetryUtils.java                    # Awaitility polling utilities
│   │   └── websocket/
│   │       └── WebSocketService.java              # OkHttp WebSocket send + ACK wait
│   └── resources/
│       ├── application.properties                 # Default profile settings
│       ├── application-dev.properties             # Dev environment
│       ├── application-uat.properties             # UAT environment
│       ├── application-prod.properties            # Production environment
│       └── log4j2.xml                             # Log4j2 rolling file configuration
└── test/
    ├── java/com/fixautomation/
    │   ├── config/
    │   │   └── CucumberSpringConfiguration.java   # @CucumberContextConfiguration + @SpringBootTest
    │   ├── hooks/
    │   │   └── CucumberHooks.java                 # Before/After hooks with Allure integration
    │   ├── runner/
    │   │   └── TestNGRunner.java                  # Cucumber-TestNG runner (parallel scenarios)
    │   ├── stepdefinitions/
    │   │   └── FixOrderStepDefinitions.java        # FIX-specific BDD step definitions
    │   └── steps/
    │       └── OrderStepDefinitions.java          # WebSocket/RabbitMQ step definitions
    └── resources/
        ├── testng.xml                             # TestNG suite (parallel, 4 threads)
        ├── config/
        │   ├── fix-config.yaml                    # FIX engine YAML config (multi-profile)
        │   ├── rabbitmq-config.yaml               # RabbitMQ YAML config (multi-profile)
        │   └── websocket-config.yaml              # WebSocket YAML config (multi-profile)
        ├── features/
        │   ├── order.feature                      # WebSocket/RabbitMQ order scenarios
        │   ├── fix_order_placement.feature        # FIX NewOrderSingle scenarios
        │   ├── fix_order_cancellation.feature     # FIX OrderCancelRequest scenarios
        │   ├── fix_order_replace.feature          # FIX OrderCancelReplaceRequest scenarios
        │   ├── fix_invalid_order.feature          # FIX invalid/rejected order scenarios
        │   └── fix_duplicate_order.feature        # FIX duplicate & status request scenarios
        └── fixmessages/
            └── quickfix.cfg                       # QuickFIX/J session configuration
```

---

## Prerequisites

- **Java 17** (or later)
- **Gradle 8+**
- A running **FIX Engine** (for FIX scenarios, set `fix.enabled=true`)
- A running **WebSocket server** accepting order requests (for WebSocket scenarios)
- A running **RabbitMQ** instance with the Management Plugin enabled (port `15672`)

---

## Configuration

### Spring Profiles

| Profile | Use case                         |
|---------|----------------------------------|
| `dev`   | Local development (FIX disabled) |
| `qa`    | QA environment with FIX engine   |
| `uat`   | UAT environment                  |
| `prod`  | Production (smoke tests only)    |

### FIX Configuration (`application.properties`)

| Property                       | Description                      | Default                    |
|--------------------------------|----------------------------------|----------------------------|
| `fix.enabled`                  | Enable/disable FIX initiator     | `false`                    |
| `fix.config-file`              | QuickFIX/J cfg file on classpath | `fixmessages/quickfix.cfg` |
| `fix.sender-comp-id`           | FIX SenderCompID                 | `CLIENT`                   |
| `fix.target-comp-id`           | FIX TargetCompID                 | `SERVER`                   |
| `fix.host`                     | FIX engine hostname              | `localhost`                |
| `fix.port`                     | FIX engine port                  | `9878`                     |
| `fix.begin-string`             | FIX protocol version             | `FIX.4.4`                  |
| `fix.response-timeout-seconds` | Wait timeout for ExecutionReport | `30`                       |
| `fix.poll-interval-millis`     | Polling interval for responses   | `500`                      |

### WebSocket & RabbitMQ Configuration

| Property                       | Description                   | Default (dev)                    |
|--------------------------------|-------------------------------|----------------------------------|
| `websocket.url`                | WebSocket endpoint            | `ws://localhost:8080/ws/orders`  |
| `rabbitmq.management.host`     | RabbitMQ Management API URL   | `http://localhost:15672`         |
| `rabbitmq.management.username` | RabbitMQ username             | `guest`                          |
| `rabbitmq.management.password` | RabbitMQ password             | `guest`                          |
| `rabbitmq.request.queue`       | Queue for order requests      | `order-request-queue`            |
| `rabbitmq.response.queue`      | Queue for order responses     | `order-response-queue`           |

---

## How to Build

```bash
gradle clean build -x test
```

---

## Running Tests

### Default (dev environment, FIX disabled)

```bash
gradle test
```

### Specific environment

```bash
gradle test -Denv=qa
gradle test -Denv=uat
gradle test -Denv=prod
```

### Run by Cucumber tag

```bash
# FIX smoke tests only
gradle test -Dcucumber.filter.tags="@smoke and @fix"

# All FIX order placement tests
gradle test -Dcucumber.filter.tags="@order_placement"

# All WebSocket/RabbitMQ tests
gradle test -Dcucumber.filter.tags="@order"

# Exclude FIX tests (WebSocket/RabbitMQ only)
gradle test -Dcucumber.filter.tags="not @fix"
```

### Enable FIX tests

```properties
# application.properties
fix.enabled=true
fix.host=your-fix-engine-host
fix.port=9878
```

---

## FIX Message Builder Usage

```java
// NewOrderSingle
String clOrdId = fixMessageBuilder.newOrderSingle()
    .symbol("AAPL")
    .side("BUY")
    .quantity(100)
    .price(150.00)
    .send();

// OrderCancelRequest
fixMessageBuilder.orderCancelRequest()
    .origClOrdId(originalClOrdId)
    .symbol("AAPL")
    .side("BUY")
    .send();

// OrderCancelReplaceRequest
fixMessageBuilder.orderCancelReplace()
    .origClOrdId(originalClOrdId)
    .symbol("AAPL")
    .side("BUY")
    .quantity(200)
    .price(155.00)
    .send();

// OrderStatusRequest
fixMessageBuilder.orderStatusRequest()
    .clOrdId(clOrdId)
    .symbol("AAPL")
    .side("BUY")
    .send();
```

---

## Adding New FIX Scenarios

1. **Create a feature file** in `src/test/resources/features/`:

```gherkin
@fix @my_scenario
Feature: My Custom FIX Scenario

  Background:
    Given FIX session is connected

  Scenario: My new scenario
    When I send a NewOrderSingle with symbol "AAPL" quantity 100
    Then ExecutionReport status should be "NEW"
```

2. **Add step definitions** (if needed) in `FixOrderStepDefinitions.java`:

```java
@When("I perform my custom step")
@Step("Perform custom step")
public void myCustomStep() {
    String clOrdId = fixMessageBuilder.newOrderSingle()
        .symbol("CUSTOM")
        .quantity(50)
        .send();
    testContext.setFixClOrdId(clOrdId);
}
```

3. **Use validators** in step definitions:

```java
fixTagValidator.assertTagEquals(message, 55, "AAPL", "Symbol");
orderStatusValidator.assertOrdStatus(message, "NEW");
executionReportValidator.assertMandatoryFields(message);
```

---

## Sample End-to-End Test

The following sample test exercises all layers of the framework:

```gherkin
@fix @smoke @e2e
Scenario: End-to-End Buy Order Validation
  Given FIX session is connected
  When I send a NewOrderSingle with symbol "AAPL" side "BUY" quantity 100 price 150.00
  Then I should receive an ExecutionReport
  And ExecutionReport status should be "NEW"
  And ExecutionReport ClOrdID should match the sent order
  And ExecutionReport should contain a valid OrderID
  And ExecutionReport mandatory fields should be present
```

When connected to a FIX engine with RabbitMQ integration enabled, you can add:

```gherkin
  And I should verify final response in RabbitMQ
  And the order response should have status "FILLED"
```

---

## Test Workflow

### FIX Engine Flow

```
┌──────────────────┐  NewOrderSingle  ┌───────────────┐
│ FixSessionManager│─────────────────▶│  FIX Engine   │
│ (QuickFIX/J      │◀──ExecutionReport│  (Acceptor)   │
│  Initiator)      │                  └───────────────┘
└────────┬─────────┘
         │ stores in
         ▼
┌──────────────────┐
│  FixResponseStore│  ConcurrentHashMap<ClOrdID, Message>
└────────┬─────────┘
         │ polled by (RetryUtils / Awaitility)
         ▼
┌─────────────────────────────┐
│  FixOrderStepDefinitions    │
└─────────────────────────────┘
         │ validates via
         ▼
┌───────────────────────────┐
│  ExecutionReportValidator │  Tags: 39, 150, 38, 14, 151, 6
│  OrderStatusValidator     │  Tags: 11, 37, 55
│  FixTagValidator          │
└───────────────────────────┘
```

### WebSocket + RabbitMQ Flow

```
┌──────────────┐    WebSocket     ┌──────────────────┐
│  Test Client  │─────────────────▶│  Order Service    │
│  (OkHttp)     │◀─── ACK ────────│  (WS Endpoint)    │
└──────────────┘                  └────────┬─────────┘
       │  REST Assured (poll)              │ Publishes response
       └──────────────────────────────────▶ RabbitMQ (Mgmt HTTP API)
```

---

## Parallel Execution

- **TestNG** suite: `parallel="methods"`, `thread-count="4"` (`testng.xml`)
- **Cucumber** `@DataProvider(parallel = true)` in `TestNGRunner`
- **TestContext** uses `@Scope("cucumber-glue")` — per-scenario isolated state
- **FixResponseStore** uses `ConcurrentHashMap` + `CopyOnWriteArrayList` for thread-safety

---

## Allure Reporting

```bash
# Generate report
allure generate build/allure-results -o build/allure-report --clean

# Open in browser
allure open build/allure-report
```

Allure attachments per scenario:
- Raw FIX messages (outbound and inbound)
- ExecutionReport model details
- FIX ClOrdID and OrderID
- RabbitMQ response payloads
- WebSocket ACK messages
- Full message log on failure

---

## Logging

Log4j2 rolling file configuration:

| File                              | Content                            |
|-----------------------------------|------------------------------------|
| `build/logs/framework.log`        | General framework / Spring logs    |
| `build/logs/fix-messages.log`     | All FIX inbound / outbound messages|
| `build/logs/test-execution.log`   | Step definitions and hooks         |

---

## CI/CD Support (GitLab CI)

The `.gitlab-ci.yml` defines a 4-stage pipeline:

| Stage            | Description                                       |
|------------------|---------------------------------------------------|
| `build`          | Compile Java sources                              |
| `test`           | Run Cucumber+TestNG scenarios (dev / qa / uat)    |
| `allure-report`  | Generate Allure HTML report from results          |
| `artifact-upload`| Archive all test reports and logs                 |

---

## Key Design Decisions

| Decision                                | Rationale                                                               |
|-----------------------------------------|-------------------------------------------------------------------------|
| **QuickFIX/J for FIX client**           | Industry-standard Java FIX library with full FIX 4.4 support           |
| **ConcurrentHashMap for response store**| Thread-safe O(1) lookup for parallel test execution                     |
| **Fluent builder for FIX messages**     | Page-Object style; readable, maintainable test code                     |
| **FixConfig @ConfigurationProperties**  | Type-safe binding; easy environment override via properties/YAML        |
| **`fix.enabled=false` default**         | Framework runs without FIX engine for WebSocket/RabbitMQ-only tests    |
| **Log4j2 with rolling files**           | Production-grade logging; separate log streams for FIX vs framework     |
| **REST Assured for RabbitMQ**           | Uses Management HTTP API; no AMQP broker dependency in test code        |
| **Awaitility for polling**              | Declarative timeout-aware polling; avoids `Thread.sleep`                |
| **Spring `@Scope("cucumber-glue")`**    | Per-scenario isolation for safe parallel execution                      |
| **SOLID principles throughout**         | Single-responsibility validators; dependency-injected builder pattern   |

---

## License

_Internal / proprietary — see your organization's licensing policy._
