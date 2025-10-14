package com.shared.common.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaginationSessionService {

    private final Map<String, PaginationSession> sessions = new ConcurrentHashMap<>();

    // default session lifetime in milliseconds (5 minutes)
    private final long defaultTtlMs = 5 * 60 * 1000;

    // maximum allowed page size to avoid abuse
    private final int maxPageSize = 500;

    public static class PaginationSession {
        public final String resourceType; // e.g., "workerUploadedData", "workerPayments"
        public final String resourceId; // optional resource id or parent id (like fileId)
        public final Map<String, String> filters;
        public final Instant createdAt;
        public final Instant expiresAt;
        public final int maxPageSize;

        public PaginationSession(String resourceType, String resourceId, Map<String, String> filters,
                                 Instant createdAt, Instant expiresAt, int maxPageSize) {
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.filters = filters;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.maxPageSize = maxPageSize;
        }
    }

    public String createSession(String resourceType, String resourceId, Map<String, String> filters,
                                Long ttlMsOverride, Integer maxPageSizeOverride) {
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Instant now = Instant.now();
        long ttl = ttlMsOverride != null ? ttlMsOverride : defaultTtlMs;
        Instant expiresAt = now.plusMillis(ttl);

        int maxSize = maxPageSizeOverride != null ? Math.min(maxPageSizeOverride, this.maxPageSize) : this.maxPageSize;

        PaginationSession session = new PaginationSession(resourceType, resourceId, filters, now, expiresAt, maxSize);

        String token = UUID.randomUUID().toString();
        sessions.put(token, session);
        return token;
    }

    public PaginationSession getSession(String token) {
        if (token == null) return null;
        PaginationSession s = sessions.get(token);
        if (s == null) return null;
        if (s.expiresAt.isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return s;
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }

}
