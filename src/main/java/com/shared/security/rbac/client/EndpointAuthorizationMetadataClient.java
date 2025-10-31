package com.shared.security.rbac.client;

import com.shared.config.SecurityProperties;
import com.shared.security.rbac.model.EndpointAuthorizationMetadata;
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

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves endpoint metadata for dynamic RBAC enforcement.
 */
public class EndpointAuthorizationMetadataClient {

    private static final Logger logger = LoggerFactory.getLogger(EndpointAuthorizationMetadataClient.class);

    private final RestTemplate restTemplate;
    private final SecurityProperties.DynamicRbacProperties properties;
    private final Map<String, CachedEntry<EndpointAuthorizationMetadata>> cache = new ConcurrentHashMap<>();

    public EndpointAuthorizationMetadataClient(RestTemplate restTemplate,
                                               SecurityProperties.DynamicRbacProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Optional<EndpointAuthorizationMetadata> fetch(String httpMethod, String requestPath) {
        if (!StringUtils.hasText(httpMethod) || !StringUtils.hasText(requestPath)) {
            logger.warn("Endpoint metadata fetch called with missing method or path");
            return Optional.empty();
        }

        String cacheKey = httpMethod.toUpperCase() + ":" + requestPath;
        CachedEntry<EndpointAuthorizationMetadata> entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.value());
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(resolveMetadataUri())
                .queryParam("method", httpMethod)
                .queryParam("path", requestPath)
                .build()
                .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            if (StringUtils.hasText(properties.getApiKey())) {
                headers.set(properties.getApiKeyHeader(), properties.getApiKey());
            }

            ResponseEntity<EndpointAuthorizationMetadata> response =
                restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), EndpointAuthorizationMetadata.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                EndpointAuthorizationMetadata metadata = response.getBody();
                cache.put(cacheKey, new CachedEntry<>(metadata, properties.getMetadataCacheTtl()));
                return Optional.of(metadata);
            }

            logger.warn("Endpoint metadata fetch returned status {} for {} {}",
                response.getStatusCode(), httpMethod, requestPath);
        } catch (RestClientException ex) {
            logger.error("Endpoint metadata fetch failed for {} {}: {}", httpMethod, requestPath, ex.getMessage());
        }

        return Optional.empty();
    }

    private String resolveMetadataUri() {
        return resolveUrl(properties.getEndpointMetadataPath());
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
