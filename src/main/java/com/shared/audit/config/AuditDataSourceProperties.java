package com.shared.audit.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dedicated data source configuration for the audit store.
 */
@ConfigurationProperties(prefix = "audit.datasource")
public class AuditDataSourceProperties extends DataSourceProperties {

    /**
     * Convenience method to build the data source with the configured settings.
     */
    public DataSource buildDataSource() {
        return this.initializeDataSourceBuilder().build();
    }
}
