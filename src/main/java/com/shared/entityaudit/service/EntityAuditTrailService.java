package com.shared.entityaudit.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import com.shared.entityaudit.exception.EntityAuditPersistenceException;
import com.shared.entityaudit.model.EntityAuditEvent;
import com.shared.entityaudit.model.EntityAuditEventRequest;
import com.shared.entityaudit.model.EntityAuditRecord;
import com.shared.entityaudit.repository.EntityAuditRepository;

/**
 * Facade used by client applications to record entity-level audit entries.
 */
public class EntityAuditTrailService {

    private static final DateTimeFormatter RECORD_NUMBER_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final EntityAuditRepository entityAuditRepository;
    private final EntityAuditHashService entityAuditHashService;
    private final Clock clock;

    public EntityAuditTrailService(EntityAuditRepository entityAuditRepository,
                                   EntityAuditHashService entityAuditHashService,
                                   Clock clock) {
        this.entityAuditRepository = Objects.requireNonNull(entityAuditRepository, "entityAuditRepository must not be null");
        this.entityAuditHashService = Objects.requireNonNull(entityAuditHashService, "entityAuditHashService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public EntityAuditRecord recordChange(EntityAuditEventRequest request) {
        ensureRecordNumber(request);
        validate(request);

        String recordNumber = request.getRecordNumber();
        String auditNumber = request.getAuditNumber().orElseGet(() -> UUID.randomUUID().toString());
        // ensure request carries generated audit number for downstream hashing and persistence
        request.setAuditNumber(auditNumber);

        String previousHash = entityAuditRepository.findLatestHash(recordNumber)
                .orElseGet(entityAuditHashService::initialHashValue);

        String hash = entityAuditHashService.computeHash(previousHash, request);
        OffsetDateTime occurredAt = request.getOccurredAt() != null ? request.getOccurredAt() : OffsetDateTime.now(clock);

        EntityAuditEvent event = EntityAuditEvent.builder()
                .occurredAt(occurredAt)
                .auditNumber(auditNumber)
                .recordNumber(recordNumber)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId().orElse(null))
                .operation(request.getOperation())
                .performedBy(request.getPerformedBy().orElse(null))
                .traceId(request.getTraceId().orElse(null))
                .metadata(request.getMetadata())
                .oldValues(request.getOldValues())
                .newValues(request.getNewValues())
                .changeSummary(request.getChangeSummary().orElse(null))
                .clientIp(request.getClientIp().orElse(null))
                .userAgent(request.getUserAgent().orElse(null))
                .prevHash(previousHash)
                .hash(hash)
                .build();

        long id = entityAuditRepository.save(event);
        return new EntityAuditRecord(id, occurredAt, auditNumber, recordNumber, hash, previousHash);
    }

    private void ensureRecordNumber(EntityAuditEventRequest request) {
        if (hasText(request.getRecordNumber())) {
            return;
        }

        String entityType = request.getEntityType();
        String entityId = request.getEntityId().orElse(null);

        if (!hasText(entityType)) {
            return;
        }

        if (!hasText(entityId)) {
            throw new EntityAuditPersistenceException("entityId must not be blank when recordNumber is not supplied");
        }

        entityAuditRepository.findRecordNumber(entityType, entityId)
                .ifPresentOrElse(request::setRecordNumber,
                        () -> request.setRecordNumber(generateRecordNumber()));
    }

    private String generateRecordNumber() {
        LocalDate today = LocalDate.now(clock);
        String datePrefix = today.format(RECORD_NUMBER_DATE);

        int nextSequence = entityAuditRepository.findLatestRecordNumberWithPrefix(datePrefix)
                .map(latest -> extractSequence(latest, datePrefix) + 1)
                .orElse(1);

        return datePrefix + String.format("%06d", nextSequence);
    }

    private int extractSequence(String recordNumber, String datePrefix) {
        if (recordNumber != null && recordNumber.startsWith(datePrefix) && recordNumber.length() > datePrefix.length()) {
            String suffix = recordNumber.substring(datePrefix.length());
            try {
                return Integer.parseInt(suffix);
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return 0;
    }

    private void validate(EntityAuditEventRequest request) {
        if (request == null) {
            throw new EntityAuditPersistenceException("EntityAuditEventRequest must not be null");
        }
        if (!hasText(request.getRecordNumber())) {
            throw new EntityAuditPersistenceException("recordNumber must not be blank");
        }
        if (!hasText(request.getEntityType())) {
            throw new EntityAuditPersistenceException("entityType must not be blank");
        }
        if (!hasText(request.getOperation())) {
            throw new EntityAuditPersistenceException("operation must not be blank");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
