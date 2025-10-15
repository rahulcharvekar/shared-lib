package com.shared.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Configuration options for the audit utility.
 */
@ConfigurationProperties(prefix = "audit")
public class AuditProperties {

    private boolean enabled;

    private String tableName = "audit_event";

    private String hashingAlgorithm = "SHA-256";

    private String initialHashValue = "0000000000000000000000000000000000000000000000000000000000000000";

    public AuditProperties() {
        // Default constructor required for configuration binding.
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        if (StringUtils.hasText(tableName)) {
            this.tableName = tableName;
        }
    }

    public String getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    public void setHashingAlgorithm(String hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public String getInitialHashValue() {
        return initialHashValue;
    }

    public void setInitialHashValue(String initialHashValue) {
        this.initialHashValue = initialHashValue;
    }
}
