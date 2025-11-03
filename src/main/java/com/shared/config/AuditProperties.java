package com.shared.config;

/**
 * Configuration properties for the audit utility.
 */
public class AuditProperties {

    private boolean enabled = false;
    private String tableName = "audit.audit_event"; // Updated to use centralized audit schema
    private String hashingAlgorithm = "SHA-256";
    private String initialHashValue = "0000000000000000000000000000000000000000000000000000000000000000";
    private String serviceName = "unknown-service"; // NEW: Service identifier
    private String sourceSchema = "unknown"; // NEW: Source schema identifier

    // Getters and setters
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
        this.tableName = tableName;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }
}
