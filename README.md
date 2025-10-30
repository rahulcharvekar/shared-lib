# Shared Audit Utility

This module provides a plug-and-play Spring Boot utility for recording application audit events in the service's primary database. It is packaged as a reusable JAR and exposes utilities for audit trail and SFTP operations.

The library includes a default `application.yml` with sample configurations that services can override.

## SFTP Utility

The library includes a generic SFTP utility for polling and downloading files from SFTP servers.

### Enabling SFTP Utility

Set `shared-lib.sftp.enabled=true` and configure the connection properties (override defaults from the library's `application.yml`):

```yaml
shared-lib:
  sftp:
    enabled: true
    host: sftp.example.com
    port: 22
    username: your_username
    password: your_password
    remote-directory: /incoming
    local-directory: /app/files
    known-hosts-file: ~/.ssh/known_hosts
    strict-host-key-checking: true
```

### Using SFTP Utility

Inject `SftpUtil` and use it to connect, list, download, and manage files:

```java
@Service
public class FileProcessor {

    private final SftpUtil sftpUtil;

    public FileProcessor(SftpUtil sftpUtil) {
        this.sftpUtil = sftpUtil;
    }

    @Scheduled(fixedDelay = 60000) // Poll every minute
    public void pollFiles() {
        try {
            sftpUtil.connect();
            List<String> files = sftpUtil.listFiles();
            for (String file : files) {
                sftpUtil.downloadFile(file, file);
                sftpUtil.moveRemoteFile(file, "/processed"); // Or deleteRemoteFile(file)
            }
        } catch (Exception e) {
            // Handle exception
        } finally {
            sftpUtil.disconnect();
        }
    }
}
```

## Audit Utility

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

The auto-configuration is **opt-in**. It activates only when the `shared-lib.audit.enabled=true` property is set in the consuming application. When enabled, the library uses the primary datasource configured in your service for audit persistence.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payment_flow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root

shared-lib:
  audit:
    enabled: true
```

### Optional tuning

| Property | Default | Description |
| --- | --- | --- |
| `shared-lib.audit.table-name` | `audit_event` | Table used to persist audit records. |
| `shared-lib.audit.hashing-algorithm` | `SHA-256` | Algorithm for chain hashing. |
| `shared-lib.audit.initial-hash-value` | `0000…000` | Seed hash when the table is empty. |

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

> **Note:** The `oldValues` and `newValues` attributes are deprecated for request auditing and are currently ignored. Use entity-level auditing if you need structured before/after payloads.

### Entity-Level Audit Helper

Enable `shared-lib.entity-audit.enabled=true` in your service to activate the entity audit module. Inject `EntityAuditHelper` for fine-grained change tracking. Besides the full `recordChange` method, you can now call `recordValueChange` for the common “single-field before/after” case—the helper resolves the logged-in user and wraps the values for you:

```java
entityAuditHelper.recordValueChange(
    "WORKER_RECEIPT",
    receipt.getId().toString(),
    "STATUS_UPDATE",
    "status",
    previousStatus,
    updatedStatus,
    "Worker receipt status changed",
    Map.of("source", "admin-console"),
    null
);
```

The helper returns an `EntityAuditRecord` that contains the generated identifiers and hash chain details should you need to correlate with API-level audits.
Record numbers are generated automatically; just supply the stable `entityId` you want future changes to stitch against.

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
