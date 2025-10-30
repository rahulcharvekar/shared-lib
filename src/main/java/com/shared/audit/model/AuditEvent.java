package com.shared.audit.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable representation of an audit event.
 */
public final class AuditEvent {

    private final OffsetDateTime occurredAt;
    private final String traceId;
    private final String userId;
    private final String action;
    private final String resourceType;
    private final String resourceId;
    private final String outcome;
    private final String clientIp;
    private final String userAgent;
    private final Map<String, Object> details;
    private final String prevHash;
    private final String hash;
    private final String responseHash;
    private final String referer;
    private final String clientSource;
    private final String requestedWith;

    private AuditEvent(Builder builder) {
        this.occurredAt = builder.occurredAt;
        this.traceId = builder.traceId;
        this.userId = builder.userId;
        this.action = builder.action;
        this.resourceType = builder.resourceType;
        this.resourceId = builder.resourceId;
        this.outcome = builder.outcome;
        this.clientIp = builder.clientIp;
        this.userAgent = builder.userAgent;
        this.details = builder.details;
        this.prevHash = builder.prevHash;
        this.hash = builder.hash;
        this.responseHash = builder.responseHash;
        this.referer = builder.referer;
        this.clientSource = builder.clientSource;
        this.requestedWith = builder.requestedWith;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Optional<String> getResourceId() {
        return Optional.ofNullable(resourceId);
    }

    public String getOutcome() {
        return outcome;
    }

    public Optional<String> getClientIp() {
        return Optional.ofNullable(clientIp);
    }

    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public String getHash() {
        return hash;
    }

    public Optional<String> getResponseHash() {
        return Optional.ofNullable(responseHash);
    }

    public Optional<String> getReferer() {
        return Optional.ofNullable(referer);
    }

    public Optional<String> getClientSource() {
        return Optional.ofNullable(clientSource);
    }

    public Optional<String> getRequestedWith() {
        return Optional.ofNullable(requestedWith);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(AuditEvent copy) {
        return builder()
                .occurredAt(copy.occurredAt)
                .traceId(copy.traceId)
                .userId(copy.userId)
                .action(copy.action)
                .resourceType(copy.resourceType)
                .resourceId(copy.resourceId)
                .outcome(copy.outcome)
                .clientIp(copy.clientIp)
                .userAgent(copy.userAgent)
                .details(copy.details)
                .prevHash(copy.prevHash)
                .hash(copy.hash)
                .responseHash(copy.responseHash)
                .referer(copy.referer)
                .clientSource(copy.clientSource)
                .requestedWith(copy.requestedWith);
    }

    public static final class Builder {
        private OffsetDateTime occurredAt;
        private String traceId;
        private String userId;
        private String action;
        private String resourceType;
        private String resourceId;
        private String outcome;
        private String clientIp;
        private String userAgent;
        private Map<String, Object> details;
        private String prevHash;
        private String hash;
        private String responseHash;
        private String referer;
        private String clientSource;
        private String requestedWith;

        private Builder() {
        }

        public Builder occurredAt(OffsetDateTime occurredAt) {
            this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = Objects.requireNonNull(traceId, "traceId must not be null");
            return this;
        }

        public Builder userId(String userId) {
            this.userId = Objects.requireNonNull(userId, "userId must not be null");
            return this;
        }

        public Builder action(String action) {
            this.action = Objects.requireNonNull(action, "action must not be null");
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = Objects.requireNonNull(resourceType, "resourceType must not be null");
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder outcome(String outcome) {
            this.outcome = Objects.requireNonNull(outcome, "outcome must not be null");
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

        public Builder details(Map<String, Object> details) {
            this.details = details;
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

        public Builder responseHash(String responseHash) {
            this.responseHash = responseHash;
            return this;
        }

        public Builder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public Builder clientSource(String clientSource) {
            this.clientSource = clientSource;
            return this;
        }

        public Builder requestedWith(String requestedWith) {
            this.requestedWith = requestedWith;
            return this;
        }

        public AuditEvent build() {
            Objects.requireNonNull(occurredAt, "occurredAt must not be null");
            Objects.requireNonNull(traceId, "traceId must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            Objects.requireNonNull(action, "action must not be null");
            Objects.requireNonNull(resourceType, "resourceType must not be null");
            Objects.requireNonNull(outcome, "outcome must not be null");
            Objects.requireNonNull(prevHash, "prevHash must not be null");
            Objects.requireNonNull(hash, "hash must not be null");
            return new AuditEvent(this);
        }
    }
}
