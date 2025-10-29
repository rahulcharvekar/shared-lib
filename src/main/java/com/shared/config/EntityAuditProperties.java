package com.shared.config;

/**
 * Configuration properties for the entity-level audit utility.
 */
public class EntityAuditProperties {

    private boolean enabled = false;
    private String tableName = "entity_audit_event";
    private String hashingAlgorithm = "SHA-256";
    private String initialHashValue = "0000000000000000000000000000000000000000000000000000000000000000";

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
}
