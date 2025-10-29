package com.shared.entityaudit.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shared.config.EntityAuditProperties;
import com.shared.entityaudit.exception.EntityAuditPersistenceException;
import com.shared.entityaudit.model.EntityAuditEventRequest;

/**
 * Computes chained hashes for entity audit events.
 */
public class EntityAuditHashService {

    private final EntityAuditProperties entityAuditProperties;
    private final ObjectMapper objectMapper;

    public EntityAuditHashService(EntityAuditProperties entityAuditProperties, ObjectMapper objectMapper) {
        this.entityAuditProperties = entityAuditProperties;
        ObjectMapper mapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.objectMapper = mapper.copy().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    public String initialHashValue() {
        String configured = entityAuditProperties.getInitialHashValue();
        if (!StringUtils.hasText(configured)) {
            throw new IllegalStateException("entity-audit.initial-hash-value must not be blank");
        }
        return configured;
    }

    public String computeHash(String prevHash, EntityAuditEventRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance(entityAuditProperties.getHashingAlgorithm());
            String serialized = serialize(prevHash, request);
            byte[] result = digest.digest(serialized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException ex) {
            throw new EntityAuditPersistenceException("Unsupported hashing algorithm: "
                    + entityAuditProperties.getHashingAlgorithm(), ex);
        }
    }

    private String serialize(String prevHash, EntityAuditEventRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("prevHash", prevHash);
            payload.put("auditNumber", request.getAuditNumber().orElse(null));
            payload.put("recordNumber", request.getRecordNumber());
            payload.put("entityType", request.getEntityType());
            payload.put("entityId", request.getEntityId().orElse(null));
            payload.put("operation", request.getOperation());
            payload.put("performedBy", request.getPerformedBy().orElse(null));
            payload.put("traceId", request.getTraceId().orElse(null));
            payload.put("metadata", request.getMetadata());
            payload.put("oldValues", request.getOldValues());
            payload.put("newValues", request.getNewValues());
            payload.put("changeSummary", request.getChangeSummary().orElse(null));
            payload.put("clientIp", request.getClientIp().orElse(null));
            payload.put("userAgent", request.getUserAgent().orElse(null));
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new EntityAuditPersistenceException("Failed to serialize entity audit payload for hashing", ex);
        }
    }
}
