package com.shared.common.jooq;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that exposes a shared {@link DSLContext} with sensible defaults for Postgres.
 */
@AutoConfiguration
@ConditionalOnClass({DSLContext.class, DefaultConfiguration.class})
@ConditionalOnBean(DataSource.class)
public class JooqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Settings jooqSettings() {
        Settings settings = new Settings();
        settings.setRenderSchema(false);
        settings.setRenderCatalog(false);
        settings.setRenderFormatted(true);
        return settings;
    }

    @Bean
    @ConditionalOnMissingBean
    public Configuration jooqConfiguration(DataSource dataSource, Settings settings) {
        DefaultConfiguration configuration = new DefaultConfiguration();
        configuration.set(dataSource);
        configuration.set(SQLDialect.POSTGRES);
        configuration.set(settings);
        return configuration;
    }

    @Bean
    @ConditionalOnMissingBean
    public DSLContext dslContext(Configuration configuration) {
        return DSL.using(configuration);
    }
}
