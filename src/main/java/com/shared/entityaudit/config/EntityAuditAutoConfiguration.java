package com.shared.entityaudit.config;

import java.time.Clock;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.config.SharedLibConfigurationProperties;
import com.shared.entityaudit.EntityAuditHelper;
import com.shared.entityaudit.repository.EntityAuditRepository;
import com.shared.entityaudit.service.EntityAuditHashService;
import com.shared.entityaudit.service.EntityAuditTrailService;
import com.shared.entityaudit.service.EntityAuditableAspect;

import javax.sql.DataSource;

/**
 * Auto configuration wiring the entity audit utility when explicitly enabled.
 */
@Configuration
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
@ConditionalOnProperty(prefix = "shared-lib.entity-audit", name = "enabled", havingValue = "true")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class EntityAuditAutoConfiguration {

    private final SharedLibConfigurationProperties sharedLibProperties;

    public EntityAuditAutoConfiguration(SharedLibConfigurationProperties sharedLibProperties) {
        this.sharedLibProperties = sharedLibProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityAuditRepository entityAuditRepository(
            ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            ObjectProvider<DataSource> dataSourceProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        NamedParameterJdbcTemplate jdbcTemplate = resolveJdbcTemplate(jdbcTemplateProvider, dataSourceProvider);
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new EntityAuditRepository(jdbcTemplate, sharedLibProperties.getEntityAudit(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityAuditHashService entityAuditHashService(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(this::defaultObjectMapper);
        return new EntityAuditHashService(sharedLibProperties.getEntityAudit(), objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityAuditTrailService entityAuditTrailService(
            EntityAuditRepository entityAuditRepository,
            EntityAuditHashService entityAuditHashService,
            ObjectProvider<Clock> clockProvider) {

        Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
        return new EntityAuditTrailService(entityAuditRepository, entityAuditHashService, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityAuditHelper entityAuditHelper(EntityAuditTrailService entityAuditTrailService) {
        return new EntityAuditHelper(entityAuditTrailService);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityAuditableAspect entityAuditableAspect(EntityAuditHelper entityAuditHelper) {
        return new EntityAuditableAspect(entityAuditHelper);
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
            throw new IllegalStateException("No NamedParameterJdbcTemplate or DataSource bean available for entity audit repository");
        }
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
