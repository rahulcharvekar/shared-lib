package com.shared.audit.config;

import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.audit.AuditHelper;
import com.shared.audit.repository.AuditEventRepository;
import com.shared.audit.service.AuditHashService;
import com.shared.audit.service.AuditTrailService;
import com.shared.config.SharedLibConfigurationProperties;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Auto configuration wiring the audit utility when explicitly enabled.
 */
@Configuration
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
@ConditionalOnProperty(prefix = "shared-lib.audit", name = "enabled", havingValue = "true")
@EnableAspectJAutoProxy
public class AuditAutoConfiguration {

    private final SharedLibConfigurationProperties sharedLibProperties;

    public AuditAutoConfiguration(SharedLibConfigurationProperties sharedLibProperties) {
        this.sharedLibProperties = sharedLibProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditEventRepository auditEventRepository(
            ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            ObjectProvider<DataSource> dataSourceProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        NamedParameterJdbcTemplate jdbcTemplate = resolveJdbcTemplate(jdbcTemplateProvider, dataSourceProvider);
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new AuditEventRepository(jdbcTemplate, sharedLibProperties.getAudit(), objectMapper);
    }
    @Bean
    @ConditionalOnMissingBean
    public AuditHashService auditHashService(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new AuditHashService(sharedLibProperties.getAudit(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditTrailService auditTrailService(
            AuditEventRepository auditEventRepository,
            AuditHashService auditHashService,
            ObjectProvider<Clock> clockProvider) {

        Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
        return new AuditTrailService(auditEventRepository, auditHashService, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditHelper auditHelper(AuditTrailService auditTrailService, ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new AuditHelper(auditTrailService, objectMapper);
    }

    private ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private NamedParameterJdbcTemplate resolveJdbcTemplate(ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
                                                           ObjectProvider<DataSource> dataSourceProvider) {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("No NamedParameterJdbcTemplate or DataSource bean available for audit repository");
        }
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
