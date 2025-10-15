package com.shared.audit.model;

import java.time.OffsetDateTime;

/**
 * Result returned after persisting an audit event.
 */
public record AuditRecord(long id, OffsetDateTime occurredAt, String hash, String prevHash) {
}
