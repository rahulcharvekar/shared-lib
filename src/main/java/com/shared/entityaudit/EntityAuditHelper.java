package com.shared.entityaudit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shared.entityaudit.model.EntityAuditEventRequest;
import com.shared.entityaudit.model.EntityAuditRecord;
import com.shared.entityaudit.service.EntityAuditTrailService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Convenience helper for recording entity audit events.
 */
public class EntityAuditHelper {

    private final EntityAuditTrailService entityAuditTrailService;

    public EntityAuditHelper(EntityAuditTrailService entityAuditTrailService) {
        this.entityAuditTrailService = entityAuditTrailService;
    }

    public EntityAuditRecord recordChange(EntityAuditEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("EntityAuditEventRequest must not be null");
        }

        if (!request.getPerformedBy().isPresent()) {
            request.setPerformedBy(resolveCurrentUserId());
        }
        if (!request.getTraceId().isPresent()) {
            request.setTraceId(resolveTraceId());
        }

        Map<String, Object> metadata = safeMetadata(request.getMetadata());
        request.setMetadata(metadata);

        HttpServletRequest httpRequest = currentRequest();
        if (httpRequest != null) {
            if (request.getClientIp().isEmpty()) {
                request.setClientIp(httpRequest.getRemoteAddr());
            }
            if (request.getUserAgent().isEmpty()) {
                request.setUserAgent(httpRequest.getHeader("User-Agent"));
            }
            metadata.putIfAbsent("referer", httpRequest.getHeader("Referer"));
            metadata.putIfAbsent("requestedWith", httpRequest.getHeader("X-Requested-With"));
        }

        return entityAuditTrailService.recordChange(request);
    }

    public EntityAuditRecord recordChange(String entityType,
                                          String recordNumber,
                                          String operation,
                                          Map<String, Object> oldValues,
                                          Map<String, Object> newValues) {
        return recordChange(entityType, recordNumber, null, operation, oldValues, newValues, null, null, null);
    }

    public EntityAuditRecord recordChange(String entityType,
                                          String recordNumber,
                                          String entityId,
                                          String operation,
                                          Map<String, Object> oldValues,
                                          Map<String, Object> newValues,
                                          Map<String, Object> metadata,
                                          String changeSummary,
                                          String auditNumber) {
        EntityAuditEventRequest request = new EntityAuditEventRequest();
        request.setEntityType(entityType);
        request.setRecordNumber(recordNumber);
        request.setEntityId(entityId);
        request.setOperation(operation);
        request.setOldValues(oldValues);
        request.setNewValues(newValues);
        request.setMetadata(safeMetadata(metadata));
        request.setChangeSummary(changeSummary);
        if (StringUtils.hasText(auditNumber)) {
            request.setAuditNumber(auditNumber);
        }

        return recordChange(request);
    }

    private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return new HashMap<>();
        }
        if (metadata instanceof HashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> copy = (Map<String, Object>) metadata;
            return copy;
        }
        return new HashMap<>(metadata);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    private String resolveTraceId() {
        return UUID.randomUUID().toString();
    }
}
