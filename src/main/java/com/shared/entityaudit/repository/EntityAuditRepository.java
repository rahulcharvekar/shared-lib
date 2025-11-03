package com.shared.entityaudit.repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.config.EntityAuditProperties;
import com.shared.entityaudit.exception.EntityAuditPersistenceException;
import com.shared.entityaudit.model.EntityAuditEvent;

/**
 * Repository handling persistence of entity audit events.
 */
public class EntityAuditRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final EntityAuditProperties entityAuditProperties;
    private final ObjectMapper objectMapper;

    public EntityAuditRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                 EntityAuditProperties entityAuditProperties) {
        this(jdbcTemplate, entityAuditProperties, createObjectMapper());
    }

    public EntityAuditRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                 EntityAuditProperties entityAuditProperties,
                                 ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityAuditProperties = entityAuditProperties;
        this.objectMapper = objectMapper;
    }

    public Optional<String> findLatestHash(String recordNumber) {
        if (recordNumber == null) {
            return Optional.empty();
        }
        String sql = """
                SELECT hash
                FROM %s
                WHERE record_number = :record_number
                ORDER BY occurred_at DESC, id DESC
                LIMIT 1
                """.formatted(entityAuditProperties.getTableName());

        Map<String, Object> params = Map.of("record_number", recordNumber);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, String.class));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> findRecordNumber(String entityType, String entityId) {
        if (entityType == null || entityId == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT record_number
                FROM %s
                WHERE entity_type = :entity_type
                  AND entity_id = :entity_id
                ORDER BY occurred_at ASC, id ASC
                LIMIT 1
                """.formatted(entityAuditProperties.getTableName());

        Map<String, Object> params = Map.of(
                "entity_type", entityType,
                "entity_id", entityId
        );

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, String.class));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> findLatestRecordNumberWithPrefix(String prefix) {
        if (prefix == null) {
            return Optional.empty();
        }

        String sql = """
                SELECT record_number
                FROM %s
                WHERE record_number LIKE :prefix
                ORDER BY record_number DESC
                LIMIT 1
                """.formatted(entityAuditProperties.getTableName());

        Map<String, Object> params = Map.of("prefix", prefix + "%");

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, String.class));
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }

    public long save(EntityAuditEvent event) {
        String sql = """
                INSERT INTO %s (
                    occurred_at,
                    audit_number,
                    record_number,
                    entity_type,
                    entity_id,
                    operation,
                    performed_by,
                    trace_id,
                    metadata,
                    old_values,
                    new_values,
                    change_summary,
                    client_ip,
                    user_agent,
                    prev_hash,
                    hash,
                    service_name,
                    source_schema,
                    source_table
                ) VALUES (
                    :occurred_at,
                    :audit_number,
                    :record_number,
                    :entity_type,
                    :entity_id,
                    :operation,
                    :performed_by,
                    :trace_id,
                    :metadata,
                    :old_values,
                    :new_values,
                    :change_summary,
                    :client_ip,
                    :user_agent,
                    :prev_hash,
                    :hash,
                    :service_name,
                    :source_schema,
                    :source_table
                )
                """.formatted(entityAuditProperties.getTableName());

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("occurred_at", toTimestamp(event.getOccurredAt()));
        parameters.addValue("audit_number", event.getAuditNumber());
        parameters.addValue("record_number", event.getRecordNumber());
        parameters.addValue("entity_type", event.getEntityType());
        parameters.addValue("entity_id", event.getEntityId().orElse(null));
        parameters.addValue("operation", event.getOperation());
        parameters.addValue("performed_by", event.getPerformedBy().orElse(null));
        parameters.addValue("trace_id", event.getTraceId().orElse(null));
        addJsonParameter(parameters, "metadata", event.getMetadata());
        addJsonParameter(parameters, "old_values", event.getOldValues());
        addJsonParameter(parameters, "new_values", event.getNewValues());
        parameters.addValue("change_summary", event.getChangeSummary().orElse(null));
        parameters.addValue("client_ip", event.getClientIp().orElse(null));
        parameters.addValue("user_agent", event.getUserAgent().orElse(null));
        parameters.addValue("prev_hash", event.getPrevHash());
        parameters.addValue("hash", event.getHash());
        parameters.addValue("service_name", entityAuditProperties.getServiceName());
        parameters.addValue("source_schema", entityAuditProperties.getSourceSchema());
        parameters.addValue("source_table", entityAuditProperties.getSourceTable());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new EntityAuditPersistenceException("Failed to retrieve generated entity audit id");
            }
            return key.longValue();
        } catch (DataAccessException ex) {
            throw new EntityAuditPersistenceException("Failed to persist entity audit event", ex);
        }
    }

    private Timestamp toTimestamp(OffsetDateTime occurredAt) {
        return Timestamp.from(occurredAt.toInstant());
    }

    private void addJsonParameter(MapSqlParameterSource parameters,
                                  String parameterName,
                                  Map<String, Object> data) {
        String json = toJson(data);
        if (json == null) {
            parameters.addValue(parameterName, null);
        } else {
            parameters.addValue(parameterName, json, Types.OTHER);
        }
    }

    private String toJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new EntityAuditPersistenceException("Failed to serialize entity audit payload to JSON", ex);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
