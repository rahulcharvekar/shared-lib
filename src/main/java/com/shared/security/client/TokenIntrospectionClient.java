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

    public TokenIntrospectionClient(RestTemplate restTemplate,
                                    SecurityProperties.IntrospectionProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Optional<TokenIntrospectionResponse> introspect(String token) {
        if (!StringUtils.hasText(token)) {
            log.debug("TokenIntrospectionClient received blank token");
            return Optional.empty();
        }

        if (!StringUtils.hasText(properties.getUrl())) {
            log.warn("Token introspection URL is not configured. Denying request.");
            return Optional.empty();
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
                return Optional.of(response.getBody());
            }

            log.warn("Token introspection returned non-success status: {}", response.getStatusCode());
            return Optional.empty();
        } catch (RestClientException ex) {
            log.error("Token introspection call failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
