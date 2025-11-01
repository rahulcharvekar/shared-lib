package com.shared.audit.repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.audit.exception.AuditPersistenceException;
import com.shared.audit.model.AuditEvent;
import com.shared.config.AuditProperties;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Repository handling persistence of audit events.
 */
public class AuditEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    public AuditEventRepository(NamedParameterJdbcTemplate jdbcTemplate, AuditProperties auditProperties) {
        this(jdbcTemplate, auditProperties, createObjectMapper());
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public AuditEventRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                AuditProperties auditProperties,
                                ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditProperties = auditProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<String> findLatestHash() {
        String sql = "SELECT hash FROM " + auditProperties.getTableName() + " ORDER BY occurred_at DESC, id DESC LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new HashMap<>(), String.class));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public long save(AuditEvent event) {
        String sql = """
                INSERT INTO %s (
                    occurred_at,
                    trace_id,
                    user_id,
                    action,
                    resource_type,
                    resource_id,
                    outcome,
                    client_ip,
                    user_agent,
                    details,
                    prev_hash,
                    hash,
                    response_hash,
                    referer,
                    client_source,
                    requested_with
                ) VALUES (
                    :occurred_at,
                    :trace_id,
                    :user_id,
                    :action,
                    :resource_type,
                    :resource_id,
                    :outcome,
                    :client_ip,
                    :user_agent,
                    :details,
                    :prev_hash,
                    :hash,
                    :response_hash,
                    :referer,
                    :client_source,
                    :requested_with
                )
                """.formatted(auditProperties.getTableName());

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("occurred_at", toTimestamp(event.getOccurredAt()));
        parameters.addValue("trace_id", event.getTraceId());
        parameters.addValue("user_id", event.getUserId());
        parameters.addValue("action", event.getAction());
        parameters.addValue("resource_type", event.getResourceType());
        parameters.addValue("resource_id", event.getResourceId().orElse(null));
        parameters.addValue("outcome", event.getOutcome());
        parameters.addValue("client_ip", event.getClientIp().orElse(null));
        parameters.addValue("user_agent", event.getUserAgent().orElse(null));
        addJsonParameter(parameters, "details", event.getDetails());
        parameters.addValue("prev_hash", event.getPrevHash());
        parameters.addValue("hash", event.getHash());
        parameters.addValue("response_hash", event.getResponseHash().orElse(null));
        parameters.addValue("referer", event.getReferer().orElse(null));
        parameters.addValue("client_source", event.getClientSource().orElse(null));
        parameters.addValue("requested_with", event.getRequestedWith().orElse(null));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new AuditPersistenceException("Failed to retrieve generated audit id");
            }
            return key.longValue();
        } catch (DataAccessException ex) {
            throw new AuditPersistenceException("Failed to persist audit event", ex);
        }
    }

    private Timestamp toTimestamp(OffsetDateTime occurredAt) {
        return Timestamp.from(occurredAt.toInstant());
    }

    private String toJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new AuditPersistenceException("Failed to serialize audit payload to JSON", ex);
        }
    }

    private void addJsonParameter(MapSqlParameterSource parameters, String parameterName, Map<String, Object> data) {
        String json = toJson(data);
        if (json == null) {
            parameters.addValue(parameterName, null);
        } else {
            parameters.addValue(parameterName, json, Types.OTHER);
        }
    }
}
