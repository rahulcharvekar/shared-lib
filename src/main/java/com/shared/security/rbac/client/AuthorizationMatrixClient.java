package com.shared.security.rbac.client;

import com.shared.config.SecurityProperties;
import com.shared.security.rbac.model.AuthorizationMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client that fetches authorization matrix snapshots from auth-service.
 */
public class AuthorizationMatrixClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMatrixClient.class);

    private final RestTemplate restTemplate;
    private final SecurityProperties.DynamicRbacProperties properties;
    private final Map<String, CachedEntry<AuthorizationMatrix>> cache = new ConcurrentHashMap<>();

    public AuthorizationMatrixClient(RestTemplate restTemplate,
                                     SecurityProperties.DynamicRbacProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Optional<AuthorizationMatrix> fetch(Long userId,
                                               Integer permissionVersion,
                                               HttpServletRequest request) {
        if (userId == null) {
            logger.warn("AuthorizationMatrixClient invoked with null userId");
            return Optional.empty();
        }

        String cacheKey = buildCacheKey(userId, permissionVersion);
        CachedEntry<AuthorizationMatrix> entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.value());
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(resolveMatrixUri())
                .build(Map.of("userId", userId));
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(request));
            ResponseEntity<AuthorizationMatrix> response =
                restTemplate.exchange(uri, HttpMethod.GET, entity, AuthorizationMatrix.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AuthorizationMatrix matrix = response.getBody();
                cache.put(cacheKey, new CachedEntry<>(matrix, properties.getMatrixCacheTtl()));
                return Optional.of(matrix);
            }
            logger.warn("Matrix fetch returned non-success status {} for user {}",
                response.getStatusCode(), userId);
        } catch (RestClientException ex) {
            logger.error("Matrix fetch failed for user {}: {}", userId, ex.getMessage());
        }

        return Optional.empty();
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (StringUtils.hasText(properties.getApiKey())) {
            headers.set(properties.getApiKeyHeader(), properties.getApiKey());
        }

        if (properties.isForwardAuthorizationHeader() && request != null) {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(auth)) {
                headers.set(HttpHeaders.AUTHORIZATION, auth);
            }
        }

        return headers;
    }

    private String resolveMatrixUri() {
        return resolveUrl(properties.getAuthorizationMatrixPath());
    }

    private String resolveUrl(String pathOrUrl) {
        if (!StringUtils.hasText(pathOrUrl)) {
            return pathOrUrl;
        }
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }
        String baseUrl = properties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            logger.warn("Dynamic RBAC baseUrl is not configured; using relative path {}", pathOrUrl);
            return pathOrUrl;
        }
        if (baseUrl.endsWith("/") && pathOrUrl.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + pathOrUrl;
        }
        if (!baseUrl.endsWith("/") && !pathOrUrl.startsWith("/")) {
            return baseUrl + "/" + pathOrUrl;
        }
        return baseUrl + pathOrUrl;
    }

    private static String buildCacheKey(Long userId, Integer permissionVersion) {
        return userId + ":" + (permissionVersion != null ? permissionVersion : "null");
    }

    private record CachedEntry<T>(T value, Instant expiresAt) {
        CachedEntry(T value, Duration ttl) {
            this(value, ttl != null && !ttl.isZero()
                ? Instant.now().plus(ttl)
                : Instant.MIN);
        }

        boolean isExpired() {
            return expiresAt != null && !expiresAt.isAfter(Instant.now());
        }
    }
}
