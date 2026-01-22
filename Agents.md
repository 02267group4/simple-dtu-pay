# DTU Pay - Microservices Project Rules

This is a **Quarkus-based Microservice** project for a mobile payment system ("DTU Pay"). 
The system enforces **Hexagonal Architecture** (Ports & Adapters) and **Domain-Driven Design (DDD)**.

## 1. Project Structure & Microservices

The architecture consists of distinct microservices running in Docker containers.

- **`customer_service/`** (Port 8081): Handles Customer registration and Token requests.
- **`merchant_service/`** (Port 8082): Handles Merchant registration.
- **`payment_service/`** (Port 8083): Orchestrates payments, talks to the Bank (SOAP), and handles Reporting.
- **`token_service/`** (Port 8084): Manages Token lifecycle (Issuance, Validation, Storage).
- **`simple_dtu_pay_client/`**: The End-to-End test suite (Cucumber).
- **`simple_dtu_pay_service/`**: ⚠️ **LEGACY MONOLITH**. Use only for reference/migration. **Do not add new features here.**

## 2. Architecture Blueprints (CRITICAL)

### Hexagonal Architecture
Inside each service (`src/main/java/dk/dtu/pay/<domain>/`), adhere to this layer separation:
1.  **`domain/model`**: Pure Java objects (Records/POJOs). No framework dependencies.
2.  **`domain/service`**: Business logic.
3.  **`application/port`**: Interfaces defining Input/Output (e.g., `RepositoryPort`).
4.  **`adapter/in`**: Entry points (REST Resources, RabbitMQ Consumers).
5.  **`adapter/out`**: External connections (Repositories, RabbitMQ Publishers, Bank Client).

### Inter-Service Communication (The "Blueprint")
Services **MUST NOT** share memory or call each other directly via Java methods. All inter-service communication is asynchronous via **RabbitMQ**.

**The Pattern:**
1.  **Request DTO:** A Java `record` defining the payload (e.g., `TokenIssueRequested`).
2.  **Publisher (Adapter Out):** Serializes DTO to JSON -> Publishes to Exchange `dtu.pay`.
3.  **Consumer (Adapter In):** Listens to specific routing key -> Deserializes -> Calls Domain Service.
4.  **Response:** If a reply is needed, the Consumer publishes a result event (e.g., `TokenIssueValidated`) back to RabbitMQ.

**Manager Report Pattern (Request/Reply):**
*   **Manager Facade:** Sends `ManagerReportRequest` (with `correlationId`) -> Waits on `CompletableFuture`.
*   **Payment Service:** Consumes Request -> Fetches Data -> Publishes `ManagerReportResponse` (with matching `correlationId`).
*   **Manager Facade:** Consumes Response -> Completes the Future -> Returns REST response.

## 3. Data Persistence
*   **No SQL Databases:** Do not implement JPA/Hibernate.
*   **In-Memory Only:** Use `ConcurrentHashMap` or `CopyOnWriteArrayList` inside Repository implementations.
*   **Mocking:** Repositories simulate a database but reside in memory within the running service container.

## 4. Coding Standards

*   **Language:** Java 21.
*   **DTOs:** Use `public record` for all Data Transfer Objects.
*   **Framework:** Quarkus.
    *   Use `@ApplicationScoped` for beans.
    *   Use `SmallRye Reactive Messaging` for RabbitMQ.
    *   Use Jackson for JSON serialization.
*   **Testing:** 
    *   Business logic is verified via `simple_dtu_pay_client` (Cucumber).
    *   Tests run against **running Docker containers**, not local mock objects.

## 5. Deployment & Execution

*   **Docker Compose:** The source of truth for running the system (`docker-compose.yml`).
*   **Ports:**
    *   Customer: 8081
    *   Merchant: 8082
    *   Payment: 8083
    *   Token: 8084
*   **Bank:** External SOAP service provided via WSDL (already integrated in `payment_service`).

## 6. Migration Tasks (Current Context)

If asked to fix "Manager Reporting" or "Token Flow":
1.  **Ignore** `simple_dtu_pay_service` code.
2.  Implement the logic in `payment_service` (for reporting) or `customer_service` (for token requests).
3.  Ensure `SimpleDTUPay.java` in the client points to the specific microservice ports (8081-8084), not 8080.