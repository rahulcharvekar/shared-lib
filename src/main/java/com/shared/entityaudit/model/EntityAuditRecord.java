package com.shared.entityaudit.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Response payload returned after recording an entity audit event.
 */
public final class EntityAuditRecord {

    private final long id;
    private final OffsetDateTime occurredAt;
    private final String auditNumber;
    private final String recordNumber;
    private final String hash;
    private final String prevHash;

    public EntityAuditRecord(long id,
                             OffsetDateTime occurredAt,
                             String auditNumber,
                             String recordNumber,
                             String hash,
                             String prevHash) {
        this.id = id;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.auditNumber = Objects.requireNonNull(auditNumber, "auditNumber must not be null");
        this.recordNumber = Objects.requireNonNull(recordNumber, "recordNumber must not be null");
        this.hash = Objects.requireNonNull(hash, "hash must not be null");
        this.prevHash = Objects.requireNonNull(prevHash, "prevHash must not be null");
    }

    public long getId() {
        return id;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getAuditNumber() {
        return auditNumber;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public String getHash() {
        return hash;
    }

    public String getPrevHash() {
        return prevHash;
    }
}
