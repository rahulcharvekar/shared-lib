package com.shared.security.client;

import com.shared.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class TokenIntrospectionClient {

    private static final Logger log = LoggerFactory.getLogger(TokenIntrospectionClient.class);

    private final RestTemplate restTemplate;
    private final SecurityProperties.IntrospectionProperties properties;
    private final boolean failOpenOnError;

    public TokenIntrospectionClient(RestTemplate restTemplate,
                                    SecurityProperties.IntrospectionProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.failOpenOnError = properties.isFailOpen();
    }

    public TokenIntrospectionResult introspect(String token) {
        if (!StringUtils.hasText(token)) {
            log.debug("TokenIntrospectionClient received blank token");
            return TokenIntrospectionResult.inactive();
        }

        if (!StringUtils.hasText(properties.getUrl())) {
            log.warn("Token introspection URL is not configured. Denying request.");
            return TokenIntrospectionResult.error(failOpenOnError);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getApiKey())) {
            headers.set(properties.getApiKeyHeader(), properties.getApiKey());
        }

        HttpEntity<TokenIntrospectionRequest> request = new HttpEntity<>(new TokenIntrospectionRequest(token), headers);
        try {
            ResponseEntity<TokenIntrospectionResponse> response =
                restTemplate.postForEntity(properties.getUrl(), request, TokenIntrospectionResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TokenIntrospectionResponse body = response.getBody();
                if (body.isActive()) {
                    return TokenIntrospectionResult.active(body);
                }
                return TokenIntrospectionResult.inactive();
            }

            if (response.getStatusCode().is5xxServerError()) {
                log.error("Token introspection returned server error status: {}", response.getStatusCode());
                return TokenIntrospectionResult.error(failOpenOnError);
            }

            log.warn("Token introspection returned non-success status: {}", response.getStatusCode());
            return TokenIntrospectionResult.inactive();
        } catch (RestClientException ex) {
            log.error("Token introspection call failed: {}", ex.getMessage());
            return TokenIntrospectionResult.error(failOpenOnError);
        }
    }

    public static final class TokenIntrospectionResult {
        public enum Status {
            ACTIVE,
            INACTIVE,
            ERROR
        }

        private final Status status;
        private final TokenIntrospectionResponse response;
        private final boolean allowOnError;

        private TokenIntrospectionResult(Status status,
                                         TokenIntrospectionResponse response,
                                         boolean allowOnError) {
            this.status = status;
            this.response = response;
            this.allowOnError = allowOnError;
        }

        public static TokenIntrospectionResult active(TokenIntrospectionResponse response) {
            return new TokenIntrospectionResult(Status.ACTIVE, response, false);
        }

        public static TokenIntrospectionResult inactive() {
            return new TokenIntrospectionResult(Status.INACTIVE, null, false);
        }

        public static TokenIntrospectionResult error(boolean allowOnError) {
            return new TokenIntrospectionResult(Status.ERROR, null, allowOnError);
        }

        public Status getStatus() {
            return status;
        }

        public Optional<TokenIntrospectionResponse> getResponse() {
            return Optional.ofNullable(response);
        }

        public boolean isAllowOnError() {
            return allowOnError;
        }
    }
}
