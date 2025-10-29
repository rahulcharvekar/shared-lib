package com.shared.entityaudit.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Request payload for recording entity audit entries.
 */
public class EntityAuditEventRequest {

    private OffsetDateTime occurredAt;
    private String auditNumber;
    private String recordNumber;
    private String entityType;
    private String entityId;
    private String operation;
    private String performedBy;
    private String traceId;
    private Map<String, Object> metadata;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private String changeSummary;
    private String clientIp;
    private String userAgent;

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Optional<String> getAuditNumber() {
        return Optional.ofNullable(auditNumber);
    }

    public void setAuditNumber(String auditNumber) {
        this.auditNumber = auditNumber;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Optional<String> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Optional<String> getPerformedBy() {
        return Optional.ofNullable(performedBy);
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Optional<String> getTraceId() {
        return Optional.ofNullable(traceId);
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getOldValues() {
        return oldValues;
    }

    public void setOldValues(Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }

    public Map<String, Object> getNewValues() {
        return newValues;
    }

    public void setNewValues(Map<String, Object> newValues) {
        this.newValues = newValues;
    }

    public Optional<String> getChangeSummary() {
        return Optional.ofNullable(changeSummary);
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public Optional<String> getClientIp() {
        return Optional.ofNullable(clientIp);
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
