package com.shared.config;

/**
 * Configuration properties for the entity-level audit utility.
 */
public class EntityAuditProperties {

    private boolean enabled = false;
    private String tableName = "audit.entity_audit_event";
    private String hashingAlgorithm = "SHA-256";
    private String initialHashValue = "0000000000000000000000000000000000000000000000000000000000000000";
    private String serviceName;
    private String sourceSchema;
    private String sourceTable;

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

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }
}
