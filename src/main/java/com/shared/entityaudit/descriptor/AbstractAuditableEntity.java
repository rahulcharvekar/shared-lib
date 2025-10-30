package com.shared.entityaudit.descriptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shared.entityaudit.model.EntityAuditAction;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * Convenience base class for entities that opt in to shared entity auditing.
 * Provides default implementations for {@link EntityAuditDescriptor} and helper
 * utilities for building audit payloads.
 *
 * @param <ID> the identifier type exposed via a {@code getId()} accessor
 */
@MappedSuperclass
public abstract class AbstractAuditableEntity<ID> implements EntityAuditDescriptor {

    @Override
    @Transient
    @JsonIgnore
    public String entityType() {
        return getClass().getSimpleName();
    }

    @Override
    @Transient
    @JsonIgnore
    public String entityId() {
        ID id = resolveEntityId();
        return id != null ? id.toString() : null;
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> auditState() {
        return Collections.emptyMap();
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> auditMetadata() {
        return Collections.emptyMap();
    }

    @Override
    @Transient
    @JsonIgnore
    public String changeSummary(EntityAuditAction action,
                                Map<String, Object> oldState,
                                Map<String, Object> newState) {
        return null;
    }

    /**
     * Utility for building an ordered map representing audit state.
     * Arguments must be supplied as key/value pairs.
     */
    protected Map<String, Object> auditStateOf(Object... keyValuePairs) {
        if (keyValuePairs == null || keyValuePairs.length == 0) {
            return Collections.emptyMap();
        }
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("auditStateOf requires an even number of arguments (key/value pairs)");
        }
        Map<String, Object> state = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            if (!(key instanceof String)) {
                throw new IllegalArgumentException("auditStateOf keys must be strings (argument index " + i + ")");
            }
            state.put((String) key, value);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private ID resolveEntityId() {
        try {
            Method getId = getClass().getMethod("getId");
            Object value = getId.invoke(this);
            return (ID) value;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
