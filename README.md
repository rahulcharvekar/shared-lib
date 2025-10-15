# Shared Audit Utility

This module provides a plug-and-play Spring Boot utility for recording application audit events in a dedicated audit database. It is packaged as a reusable JAR and exposes a single `AuditTrailService` entry point that other microservices can inject and call.

## Including the Library in Your Project

To use this shared library in your Spring Boot project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.shared</groupId>
    <artifactId>shared-lib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Ensure the JAR is available in your Maven repository. For local development, install it using `mvn install` in this project.

## Enabling the audit utility

The auto-configuration is **opt-in**. It activates only when the `audit.enabled=true` property is set in the consuming application. When enabled, the library uses the primary datasource configured in your service for audit persistence.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payment_flow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root

audit:
  enabled: true
```

### Optional tuning

| Property | Default | Description |
| --- | --- | --- |
| `audit.table-name` | `audit_event` | Table used to persist audit records. |
| `audit.hashing-algorithm` | `SHA-256` | Algorithm for chain hashing. |
| `audit.initial-hash-value` | `0000…000` | Seed hash when the table is empty. |

## Usage

Inject `AuditTrailService` and call `recordEvent` with the required metadata.

```java
@Service
class PaymentService {

    private final AuditTrailService auditTrailService;

    PaymentService(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    void updateStatus(Payment payment) {
        // ... business logic ...

        AuditEventRequest event = new AuditEventRequest();
        event.setTraceId(payment.traceId());
        event.setUserId(payment.updatedBy());
        event.setAction("PAYMENT_STATUS_UPDATED");
        event.setResourceType("PAYMENT");
        event.setResourceId(payment.id());
        event.setOutcome("SUCCESS");
        event.setDetails(Map.of("status", payment.status()));

        auditTrailService.recordEvent(event);
    }
}
```

`recordEvent` returns an `AuditRecord` containing the generated identifier, event timestamp, and the chain hashes that were persisted.

### Automatic Auditing with @Auditable Annotation

For automatic auditing, annotate methods with `@Auditable`. The library includes an AspectJ aspect that intercepts annotated methods and records audit events automatically.

```java
@Auditable(action = "PAYMENT_STATUS_UPDATED", resourceType = "PAYMENT", resourceId = "#payment.id", details = "{'status': #payment.status()}")
public void updateStatus(Payment payment) {
    // ... business logic ...
}
```

The annotation supports SpEL expressions for dynamic values. Available attributes:

- `action`: The action performed.
- `resourceType`: The type of resource.
- `resourceId`: The resource ID (SpEL supported).
- `details`: Additional details as a map (SpEL supported).
- `oldValues`: Previous state (SpEL supported).
- `newValues`: Updated state (SpEL supported).

## Schema

The library expects an `audit_event` table with the columns below. The same structure is used in the integration tests.

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `BIGINT` (auto increment) | Primary key. |
| `occurred_at` | `DATETIME(6)` | Event timestamp (UTC). |
| `trace_id` | `VARCHAR(64)` | Correlates to the call trace or span. |
| `user_id` | `VARCHAR(128)` | Authenticated user identifier. |
| `action` | `VARCHAR(128)` | Operation descriptor. |
| `resource_type` | `VARCHAR(64)` | Domain entity type. |
| `resource_id` | `VARCHAR(128)` | Domain entity identifier (nullable). |
| `outcome` | `VARCHAR(16)` | e.g. SUCCESS / FAILURE. |
| `client_ip` | `VARCHAR(64)` | Optional remote address. |
| `user_agent` | `VARCHAR(256)` | Optional agent string. |
| `details` | `JSON` | Arbitrary payload (nullable). |
| `old_values` | `JSON` | Previous state (nullable). |
| `new_values` | `JSON` | Updated state (nullable). |
| `prev_hash` | `VARCHAR(64)` | Chain hash of the previous event. |
| `hash` | `VARCHAR(64)` | Chain hash of the current event. |
| `response_hash` | `VARCHAR(64)` | Optional response payload hash. |
| `referer` | `VARCHAR(256)` | Optional HTTP referer. |
| `client_source` | `VARCHAR(64)` | Optional channel identifier. |
| `requested_with` | `VARCHAR(64)` | Optional transport hint. |

### Table Creation Script

A SQL script to create the table is provided in `create_audit_table.sql`.

## Testing

`mvn test`

The suite includes:

- `AuditTrailServiceIntegrationTest` – verifies chained persistence against an in-memory H2 database.
- `AuditAutoConfigurationDisabledTest` – ensures the auto-configuration stays dormant unless explicitly enabled.
