package com.shared.entityaudit.descriptor;

import java.util.Collections;
import java.util.Map;

import com.shared.entityaudit.model.EntityAuditAction;

/**
 * Contract that domain entities can implement to enable automatic entity-level auditing.
 */
public interface EntityAuditDescriptor {

    /**
     * Logical entity type used for auditing (e.g., WORKER_RECEIPT).
     */
    String entityType();

    /**
     * Stable identifier for the entity instance (e.g., primary key value).
     */
    String entityId();

    /**
     * Returns a map representation of the entity's auditable state.
     * Implementations should return immutable or defensive copies.
     */
    Map<String, Object> auditState();

    /**
     * Optional metadata that should be attached to the audit event.
     */
    default Map<String, Object> auditMetadata() {
        return Collections.emptyMap();
    }

    /**
     * Optional human-readable summary per operation.
     *
     * @param action    the lifecycle action that triggered the audit
     * @param oldState  previous state (may be null)
     * @param newState  new state (may be null)
     * @return summary string or null
     */
    default String changeSummary(EntityAuditAction action,
                                 Map<String, Object> oldState,
                                 Map<String, Object> newState) {
        return null;
    }
}

