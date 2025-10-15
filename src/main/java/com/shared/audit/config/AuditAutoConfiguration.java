package com.shared.audit.config;

import java.time.Clock;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.audit.repository.AuditEventRepository;
import com.shared.audit.service.AuditHashService;
import com.shared.audit.service.AuditTrailService;
import com.shared.utilities.AuditHelper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto configuration wiring the audit utility when explicitly enabled.
 */
@Configuration
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
@ConditionalOnProperty(prefix = "audit", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({AuditProperties.class})
@EnableAspectJAutoProxy
public class AuditAutoConfiguration {

    private final AuditProperties auditProperties;

    public AuditAutoConfiguration(AuditProperties auditProperties) {
        this.auditProperties = auditProperties;
    }

    @Bean(name = "auditNamedParameterJdbcTemplate")
    @ConditionalOnMissingBean(name = "auditNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate auditNamedParameterJdbcTemplate(
            @Qualifier("dataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditEventRepository auditEventRepository(
            @Qualifier("auditNamedParameterJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new AuditEventRepository(jdbcTemplate, auditProperties, objectMapper);
    }
    @Bean
    @ConditionalOnMissingBean
    public AuditHashService auditHashService(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new AuditHashService(auditProperties, objectMapper);
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
}
