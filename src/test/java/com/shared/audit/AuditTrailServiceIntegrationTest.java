package com.shared.audit;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.shared.audit.model.AuditEventRequest;
import com.shared.audit.model.AuditRecord;
import com.shared.audit.config.AuditAutoConfiguration;
import com.shared.audit.service.AuditTrailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailServiceIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class,
                    JacksonAutoConfiguration.class,
                    AuditAutoConfiguration.class))
            .withPropertyValues(
                    "audit.enabled=true",
                    "spring.datasource.url=jdbc:h2:mem:auditdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
                    "spring.datasource.username=sa",
                    "spring.datasource.password=",
                    "spring.datasource.driver-class-name=org.h2.Driver"
            );

    @Test
    void recordEvent_persistsDataAndChainsHash() {
        contextRunner.run(context -> {
            AuditTrailService auditTrailService = context.getBean(AuditTrailService.class);
            DataSource auditDataSource = context.getBean("dataSource", DataSource.class);
            NamedParameterJdbcTemplate auditJdbcTemplate = context.getBean("auditNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class);

            createAuditTable(auditDataSource);

            AuditEventRequest request = buildRequest("TRACE-1");

            AuditRecord record = auditTrailService.recordEvent(request);

            assertThat(record.id()).isPositive();
            assertThat(record.hash()).isNotBlank();
            assertThat(record.prevHash()).isNotBlank();

            var row = auditJdbcTemplate.getJdbcTemplate()
                    .queryForMap("SELECT * FROM audit_event WHERE id = ?", record.id());

            assertThat(row.get("TRACE_ID")).isEqualTo("TRACE-1");
            assertThat(row.get("HASH")).isEqualTo(record.hash());
            assertThat(row.get("PREV_HASH")).isEqualTo(record.prevHash());
            assertThat(row.get("DETAILS")).asString().contains("resource", "value");

            AuditEventRequest secondRequest = buildRequest("TRACE-2");
            AuditRecord secondRecord = auditTrailService.recordEvent(secondRequest);

            assertThat(secondRecord.prevHash()).isEqualTo(record.hash());
        });
    }

    private AuditEventRequest buildRequest(String traceId) {
        AuditEventRequest request = new AuditEventRequest();
        request.setTraceId(traceId);
        request.setUserId("user-42");
        request.setAction("UPDATED");
        request.setResourceType("PAYMENT");
        request.setResourceId("PAY-123");
        request.setOutcome("SUCCESS");
        request.setClientIp("127.0.0.1");
        request.setUserAgent("JUnit");
        request.setDetails(Map.of("resource", "payment", "value", 150));
        request.setOldValues(Map.of("status", "PENDING"));
        request.setNewValues(Map.of("status", "COMPLETED"));
        request.setResponseHash("response-hash");
        request.setReferer("https://example.com");
        request.setClientSource("WEB");
        request.setRequestedWith("XMLHttpRequest");
        return request;
    }

    private void createAuditTable(DataSource dataSource) {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS audit_event");
            statement.execute("""
                    CREATE TABLE audit_event (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        occurred_at TIMESTAMP(6) NOT NULL,
                        trace_id VARCHAR(64) NOT NULL,
                        user_id VARCHAR(128) NOT NULL,
                        action VARCHAR(128) NOT NULL,
                        resource_type VARCHAR(64) NOT NULL,
                        resource_id VARCHAR(128),
                        outcome VARCHAR(16) NOT NULL,
                        client_ip VARCHAR(64),
                        user_agent VARCHAR(256),
                        details VARCHAR(2048),
                        old_values VARCHAR(2048),
                        new_values VARCHAR(2048),
                        prev_hash VARCHAR(64) NOT NULL,
                        hash VARCHAR(64) NOT NULL,
                        response_hash VARCHAR(64),
                        referer VARCHAR(256),
                        client_source VARCHAR(64),
                        requested_with VARCHAR(64)
                    )
                    """);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to prepare audit_event table", ex);
        }
    }
}
