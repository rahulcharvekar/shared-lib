package com.shared.entityaudit.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable representation of an entity audit event.
 */
public final class EntityAuditEvent {

    private final OffsetDateTime occurredAt;
    private final String auditNumber;
    private final String recordNumber;
    private final String entityType;
    private final String entityId;
    private final String operation;
    private final String performedBy;
    private final String traceId;
    private final Map<String, Object> metadata;
    private final Map<String, Object> oldValues;
    private final Map<String, Object> newValues;
    private final String changeSummary;
    private final String clientIp;
    private final String userAgent;
    private final String prevHash;
    private final String hash;

    private EntityAuditEvent(Builder builder) {
        this.occurredAt = builder.occurredAt;
        this.auditNumber = builder.auditNumber;
        this.recordNumber = builder.recordNumber;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.operation = builder.operation;
        this.performedBy = builder.performedBy;
        this.traceId = builder.traceId;
        this.metadata = builder.metadata;
        this.oldValues = builder.oldValues;
        this.newValues = builder.newValues;
        this.changeSummary = builder.changeSummary;
        this.clientIp = builder.clientIp;
        this.userAgent = builder.userAgent;
        this.prevHash = builder.prevHash;
        this.hash = builder.hash;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getAuditNumber() {
        return auditNumber;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public String getEntityType() {
        return entityType;
    }

    public Optional<String> getEntityId() {
        return Optional.ofNullable(entityId);
    }

    public String getOperation() {
        return operation;
    }

    public Optional<String> getPerformedBy() {
        return Optional.ofNullable(performedBy);
    }

    public Optional<String> getTraceId() {
        return Optional.ofNullable(traceId);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getOldValues() {
        return oldValues;
    }

    public Map<String, Object> getNewValues() {
        return newValues;
    }

    public Optional<String> getChangeSummary() {
        return Optional.ofNullable(changeSummary);
    }

    public Optional<String> getClientIp() {
        return Optional.ofNullable(clientIp);
    }

    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getHash() {
        return hash;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
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
        private String prevHash;
        private String hash;

        private Builder() {
        }

        public Builder occurredAt(OffsetDateTime occurredAt) {
            this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
            return this;
        }

        public Builder auditNumber(String auditNumber) {
            this.auditNumber = Objects.requireNonNull(auditNumber, "auditNumber must not be null");
            return this;
        }

        public Builder recordNumber(String recordNumber) {
            this.recordNumber = Objects.requireNonNull(recordNumber, "recordNumber must not be null");
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = Objects.requireNonNull(entityType, "entityType must not be null");
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = Objects.requireNonNull(operation, "operation must not be null");
            return this;
        }

        public Builder performedBy(String performedBy) {
            this.performedBy = performedBy;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder oldValues(Map<String, Object> oldValues) {
            this.oldValues = oldValues;
            return this;
        }

        public Builder newValues(Map<String, Object> newValues) {
            this.newValues = newValues;
            return this;
        }

        public Builder changeSummary(String changeSummary) {
            this.changeSummary = changeSummary;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder prevHash(String prevHash) {
            this.prevHash = Objects.requireNonNull(prevHash, "prevHash must not be null");
            return this;
        }

        public Builder hash(String hash) {
            this.hash = Objects.requireNonNull(hash, "hash must not be null");
            return this;
        }

        public EntityAuditEvent build() {
            Objects.requireNonNull(occurredAt, "occurredAt must not be null");
            Objects.requireNonNull(auditNumber, "auditNumber must not be null");
            Objects.requireNonNull(recordNumber, "recordNumber must not be null");
            Objects.requireNonNull(entityType, "entityType must not be null");
            Objects.requireNonNull(operation, "operation must not be null");
            Objects.requireNonNull(prevHash, "prevHash must not be null");
            Objects.requireNonNull(hash, "hash must not be null");
            return new EntityAuditEvent(this);
        }
    }
}
