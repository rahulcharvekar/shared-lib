package com.shared.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shared.audit.model.AuditEventRequest;
import com.shared.audit.service.AuditTrailService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Convenience helper for creating and recording audit events.
 */
public class AuditHelper {

    private final AuditTrailService auditTrailService;
    private final ObjectMapper objectMapper;

    public AuditHelper(AuditTrailService auditTrailService, ObjectMapper objectMapper) {
        this.auditTrailService = auditTrailService;
        this.objectMapper = objectMapper;
    }

    public void recordAudit(String action,
                            String resourceType,
                            String resourceId,
                            String outcome,
                            Map<String, Object> details) {
        AuditEventRequest event = new AuditEventRequest();
        event.setTraceId(UUID.randomUUID().toString());
        event.setUserId(resolveCurrentUserId());
        event.setAction(action);
        event.setResourceType(resourceType);
        event.setResourceId(resourceId);
        event.setOutcome(outcome);
        if (details != null && !details.isEmpty()) {
            event.setDetails(details);
            // Compute response hash if result is present
            if (details.containsKey("result")) {
                try {
                    String resultJson = objectMapper.writeValueAsString(details.get("result"));
                    String responseHash = computeHash(resultJson);
                    event.setResponseHash(responseHash);
                } catch (JsonProcessingException e) {
                    // Ignore, responseHash remains null
                }
            }
        }

        // Populate HTTP-related fields from current request
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            event.setClientIp(request.getRemoteAddr());
            event.setUserAgent(request.getHeader("User-Agent"));
            event.setReferer(request.getHeader("Referer"));
            event.setRequestedWith(request.getHeader("X-Requested-With"));
            // clientSource can be set if there's a specific header, e.g., request.getHeader("X-Client-Source")
            // For now, leaving it null or set based on logic
        }

        auditTrailService.recordEvent(event);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
