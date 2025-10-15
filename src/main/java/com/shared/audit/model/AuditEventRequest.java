package com.shared.audit.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Request payload used for recording audit events.
 */
public class AuditEventRequest {

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
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private String responseHash;
    private String referer;
    private String clientSource;
    private String requestedWith;

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Optional<String> getResourceId() {
        return Optional.ofNullable(resourceId);
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
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

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
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

    public Optional<String> getResponseHash() {
        return Optional.ofNullable(responseHash);
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public Optional<String> getReferer() {
        return Optional.ofNullable(referer);
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Optional<String> getClientSource() {
        return Optional.ofNullable(clientSource);
    }

    public void setClientSource(String clientSource) {
        this.clientSource = clientSource;
    }

    public Optional<String> getRequestedWith() {
        return Optional.ofNullable(requestedWith);
    }

    public void setRequestedWith(String requestedWith) {
        this.requestedWith = requestedWith;
    }
}
