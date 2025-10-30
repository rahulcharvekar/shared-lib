package com.shared.audit.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shared.audit.exception.AuditPersistenceException;
import com.shared.audit.model.AuditEventRequest;
import com.shared.config.AuditProperties;

import org.springframework.util.StringUtils;

/**
 * Computes chained hashes for audit events.
 */
public class AuditHashService {

    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    public AuditHashService(AuditProperties auditProperties, ObjectMapper objectMapper) {
        this.auditProperties = auditProperties;
        ObjectMapper mapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.objectMapper = mapper.copy().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    public String initialHashValue() {
        String configured = auditProperties.getInitialHashValue();
        if (!StringUtils.hasText(configured)) {
            throw new IllegalStateException("audit.initial-hash-value must not be blank");
        }
        return configured;
    }

    public String computeHash(String prevHash, AuditEventRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance(auditProperties.getHashingAlgorithm());
            String serialized = serialize(prevHash, request);
            byte[] result = digest.digest(serialized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException ex) {
            throw new AuditPersistenceException("Unsupported hashing algorithm: " + auditProperties.getHashingAlgorithm(), ex);
        }
    }

    private String serialize(String prevHash, AuditEventRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("prevHash", prevHash);
            payload.put("traceId", request.getTraceId());
            payload.put("userId", request.getUserId());
            payload.put("action", request.getAction());
            payload.put("resourceType", request.getResourceType());
            payload.put("resourceId", request.getResourceId().orElse(null));
            payload.put("outcome", request.getOutcome());
            payload.put("clientIp", request.getClientIp().orElse(null));
            payload.put("userAgent", request.getUserAgent().orElse(null));
            payload.put("referer", request.getReferer().orElse(null));
            payload.put("clientSource", request.getClientSource().orElse(null));
            payload.put("requestedWith", request.getRequestedWith().orElse(null));
            payload.put("details", request.getDetails());
            payload.put("responseHash", request.getResponseHash().orElse(null));
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AuditPersistenceException("Failed to serialize audit payload for hashing", ex);
        }
    }
}
