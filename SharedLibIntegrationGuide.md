# Integrating Shared-Lib as a JAR Dependency in a Spring Boot Microservice

This guide provides a step-by-step process to integrate the `shared-lib` JAR into any Spring Boot microservice. The library offers optional features like audit logging, JWT-based security, SFTP utilities, global exception handling, and common utilities, all designed for minimal impact.

## Prerequisites
- Spring Boot application (version 3.x recommended, compatible with the library's Spring Boot 3.2.5).
- Java 17+.
- Maven or Gradle for dependency management.
- The `shared-lib` JAR must be available (e.g., via local Maven repo, Nexus, or build from source). If unpublished, run `mvn clean install` in the `shared-lib` project to install locally.

## Step 1: Add the Dependency
Update your microservice's `pom.xml` (for Maven) or `build.gradle` (for Gradle) to include the `shared-lib` dependency.

### For Maven (`pom.xml`):
Add under `<dependencies>`:
```xml
<dependency>
    <groupId>com.shared</groupId>
    <artifactId>shared-lib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### For Gradle (`build.gradle`):
Add to `dependencies` block:
```gradle
implementation 'com.shared:shared-lib:0.0.1-SNAPSHOT'
```

- **Action:** Run `mvn clean install` (Maven) or `./gradlew build` (Gradle) to resolve dependencies.
- **Impact:** No code changes; just a build file update.

## Step 2: Configure Properties
The library uses conditional auto-configuration, so features are opt-in. Override defaults in your `application.yml` or `application.properties`.

Add to `application.yml`:
```yaml
shared-lib:
  audit:
    enabled: true  # Enable audit logging (requires DataSource)
    table-name: audit_event
    hashing-algorithm: SHA-256
    initial-hash-value: 0000000000000000000000000000000000000000000000000000000000000000

  sftp:
    enabled: true  # Enable SFTP operations
    host: your-sftp-host
    port: 22
    username: your-username
    password: ${SFTP_PASSWORD}  # Use env vars for security
    remote-directory: /your/remote/path
    local-directory: /tmp/sftp
    known-hosts-file: ~/.ssh/known_hosts
    strict-host-key-checking: true

  security:
    enabled: true  # Enable JWT-based security
    permitted-paths:
      - /actuator/**
      - /v3/api-docs/**
      - /swagger-ui/**
      - /your/public/endpoints/**
```

- **Action:** Customize values; use environment variables for secrets.
- **Impact:** Minimal; only add properties for needed features. Disabled by default.

## Step 3: Enable Features in Code (If Required)
Most features auto-activate via properties, but some need annotations or injections.

### For Audit Logging
- **Requirement:** DataSource bean (e.g., from `spring-boot-starter-data-jpa`).
- **Annotation-Based Auditing:** Use `@Auditable` on methods for automatic logging.
  ```java
  import com.shared.common.annotation.Auditable;

  @Service
  public class UserService {
      @Auditable(action = "CREATE_USER", resourceType = "User", resourceId = "#result.id", details = "{'email': #user.email}")
      public User createUser(User user) {
          // Method logic
          return userRepository.save(user);
      }
  }
  ```
  - Supports SpEL for dynamic values (e.g., `#result.id` for resourceId).
- **Manual Auditing:** Inject `AuditTrailService` or `AuditHelper`.
  ```java
  @Autowired
  private AuditTrailService auditTrailService;

  public void someMethod() {
      auditTrailService.logEvent("ACTION", userId, Map.of("details", "data"));
  }
  ```
- **Impact:** Add annotation to methods or inject services where needed.

### For Security (JWT)
- **Action:** Add `@EnableSharedSecurity` to your main class.
  ```java
  @SpringBootApplication
  @EnableSharedSecurity
  public class YourMicroserviceApplication {
      public static void main(String[] args) {
          SpringApplication.run(YourMicroserviceApplication.class, args);
      }
  }
  ```
- **Impact:** One annotation; secures endpoints except permitted paths.

### For SFTP Utilities
- **Action:** Inject `SftpUtil`.
  ```java
  @Autowired
  private SftpUtil sftpUtil;

  public void upload() {
      sftpUtil.uploadFile(localPath, remotePath);
  }
  ```
- **Impact:** Inject and call as needed.

### For Global Exception Handling
- **Action:** None required; auto-applies to controllers.
- **Impact:** Zero changes; consistent error responses.

### For Other Utilities
- **Action:** Import and use classes from `com.shared.common` (e.g., DTOs, annotations).
- **Impact:** Direct usage without modifications.

## Step 4: Test and Validate
- Run your microservice and verify features (e.g., check audit logs in DB, test secured endpoints).
- Use library tests as reference (e.g., auto-configuration tests).
- Handle conflicts: Disable library features if your service has overlapping logic.

## Additional Notes
- **Dependencies:** Ensure your POM includes starters like `spring-boot-starter-web` and `spring-boot-starter-data-jpa` for full functionality.
- **Security:** Avoid hardcoding secrets; use profiles or external config.
- **Updates:** Check for changes in library versions.
- **Minimal Changes:** With dependency and properties, most features work without code edits. Total changes: 1-5 lines in build file, 10-20 in config, optional annotations/injections.

For issues, refer to library source or provide microservice details.</content>
<parameter name="filePath">/Users/rahulcharvekar/Documents/Repos/LBE/PaymentReconciliation/shared-lib/SharedLibIntegrationGuide.md
