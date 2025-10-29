package com.shared.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for the dedicated auditing datasource.
 */
public class AuditingProperties {

    private boolean enabled = false;

    @NestedConfigurationProperty
    private final DataSourceProperties datasource = new DataSourceProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DataSourceProperties getDatasource() {
        return datasource;
    }
}
