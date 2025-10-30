package com.shared.audit.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

import com.shared.audit.exception.AuditPersistenceException;
import com.shared.audit.model.AuditEvent;
import com.shared.audit.model.AuditEventRequest;
import com.shared.audit.model.AuditRecord;
import com.shared.audit.repository.AuditEventRepository;

/**
 * Facade used by client applications to record audit trail entries.
 */
public class AuditTrailService {

    private final AuditEventRepository auditEventRepository;
    private final AuditHashService auditHashService;
    private final Clock clock;

    public AuditTrailService(AuditEventRepository auditEventRepository,
                             AuditHashService auditHashService,
                             Clock clock) {
        this.auditEventRepository = Objects.requireNonNull(auditEventRepository, "auditEventRepository must not be null");
        this.auditHashService = Objects.requireNonNull(auditHashService, "auditHashService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public AuditRecord recordEvent(AuditEventRequest request) {
        validate(request);

        String previousHash = auditEventRepository.findLatestHash()
                .orElseGet(auditHashService::initialHashValue);

        String hash = auditHashService.computeHash(previousHash, request);
        OffsetDateTime occurredAt = request.getOccurredAt() != null ? request.getOccurredAt() : OffsetDateTime.now(clock);

        AuditEvent event = AuditEvent.builder()
                .occurredAt(occurredAt)
                .traceId(request.getTraceId())
                .userId(request.getUserId())
                .action(request.getAction())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId().orElse(null))
                .outcome(request.getOutcome())
                .clientIp(request.getClientIp().orElse(null))
                .userAgent(request.getUserAgent().orElse(null))
                .details(request.getDetails())
                .prevHash(previousHash)
                .hash(hash)
                .responseHash(request.getResponseHash().orElse(null))
                .referer(request.getReferer().orElse(null))
                .clientSource(request.getClientSource().orElse(null))
                .requestedWith(request.getRequestedWith().orElse(null))
                .build();

        long id = auditEventRepository.save(event);
        return new AuditRecord(id, occurredAt, hash, previousHash);
    }

    private void validate(AuditEventRequest request) {
        if (request == null) {
            throw new AuditPersistenceException("AuditEventRequest must not be null");
        }
        if (!hasText(request.getTraceId())) {
            throw new AuditPersistenceException("traceId must not be blank");
        }
        if (!hasText(request.getUserId())) {
            throw new AuditPersistenceException("userId must not be blank");
        }
        if (!hasText(request.getAction())) {
            throw new AuditPersistenceException("action must not be blank");
        }
        if (!hasText(request.getResourceType())) {
            throw new AuditPersistenceException("resourceType must not be blank");
        }
        if (!hasText(request.getOutcome())) {
            throw new AuditPersistenceException("outcome must not be blank");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
