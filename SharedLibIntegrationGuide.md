# Integrating Shared-Lib as a JAR Dependency in a Spring Boot Microservice

This guide walks through wiring the `shared-lib` JAR into a Spring Boot microservice. The library ships opt-in modules for auditing, JWT security, SFTP, secure pagination, file uploads, common filters, and DTOs. Each module activates only when you set the corresponding properties, keeping the footprint small.

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
Each feature is guarded behind configuration flags. Override only what you need in `application.yml` (or `application.properties`):
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
    enabled: true  # Enable JWT-based security filter chain
    permitted-paths:
      - /actuator/**
      - /v3/api-docs/**
      - /swagger-ui/**
      - /your/public/endpoints/**
    introspection:
      enabled: true
      url: ${AUTH_SERVICE_BASE_URL:http://auth-service:8080}/internal/auth/introspect
      api-key: ${INTERNAL_API_KEY}          # Shared secret for service-to-service auth
      api-key-header: X-Internal-Api-Key    # Defaults to X-Internal-Api-Key
      connect-timeout: 2s                   # Spring Boot duration format
      read-timeout: 2s

  file-upload:
    enabled: true
    base-dir: /data/uploads

app:
  jwt:
    secret: ${JWT_SECRET}         # 32+ byte hex/base64 string
    issuer: payment-flow-api
    audience: payment-flow-clients
```

- **Action:** Customize only the sections you intend to use. Secrets should come from environment variables or an external vault.
- **Impact:** No application code needed; features remain dormant until enabled.
- **Stateless validation:** With introspection enabled the shared JWT filter calls `auth-service` on every request to confirm `permissionVersion`, account status, and token expiry before letting the request through. Distribute the `INTERNAL_API_KEY` via your secret manager.

## Step 3: Enable Features in Code (If Required)
Auto-configuration covers bean creation. You only need to touch code when you want to consume a service or add annotations.

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
- **Action:** Setting `shared-lib.security.enabled=true` activates the stateless security `SecurityFilterChain`.
- **Swagger integration:** If you want the bundled OpenAPI security config, add `@EnableSharedSecurity` to any `@Configuration` class.
  ```java
  @SpringBootApplication
  @EnableSharedSecurity
  public class YourMicroserviceApplication {
      public static void main(String[] args) {
          SpringApplication.run(YourMicroserviceApplication.class, args);
      }
  }
  ```
- **Impact:** All endpoints (other than `permitted-paths`) expect a valid JWT signed with your configured secret.
- **Tip:** Register additional Spring Security customizers if you need method security, role mapping, etc.

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

### For File Upload Utility
- **Action:** Inject `FileStorageService`; no manual bean definition required.
  ```java
  @Service
  public class ReceiptUploadService {
      private final FileStorageService fileStorageService;

      public ReceiptUploadService(FileStorageService fileStorageService) {
          this.fileStorageService = fileStorageService;
      }

      public FileMetadata store(MultipartFile file) throws IOException {
          return fileStorageService.storeFile(file, "receipts", file.getOriginalFilename());
      }
  }
  ```
- **Impact:** Directory is controlled by `shared-lib.file-upload.base-dir`.

### For Global Exception Handling
- **Action:** None required; auto-applies to controllers.
- **Impact:** Zero changes; consistent error responses.

### For Other Utilities
- **Action:** Import and use classes from `com.shared.common` (e.g., DTOs, annotations).
- **Impact:** Direct usage without modifications.

> **Note on component scanning:** Some supporting filters (`RequestIdFilter`, `SignatureVerificationFilter`) are declared as `@Component`. Ensure your application’s component scan includes `com.shared` (e.g., `@SpringBootApplication(scanBasePackages = {"com.example", "com.shared"})`) if you rely on them.

## Step 4: Test and Validate
- Run your microservice and verify features (e.g., check audit logs in DB, test secured endpoints).
- Use library tests as reference (e.g., auto-configuration tests).
- Handle conflicts: Disable library features if your service has overlapping logic.

## Additional Notes
- **Dependencies:** Ensure your POM includes starters like `spring-boot-starter-web` and `spring-boot-starter-data-jpa` for full functionality.
- **Security:** Avoid hardcoding secrets; use profiles or external config.
- **Updates:** Check for changes in library versions.
- **Minimal Changes:** With dependency and properties, most features work without code edits. Total changes: 1-5 lines in build file, 10-20 in config, optional annotations/injections.

For issues, refer to the library source or provide microservice details.</content>
