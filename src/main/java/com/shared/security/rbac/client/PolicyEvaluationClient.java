package com.shared.security.rbac.client;

import com.shared.config.SecurityProperties;
import com.shared.security.rbac.model.PolicyEvaluationRequest;
import com.shared.security.rbac.model.PolicyEvaluationResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Delegates policy evaluation to auth-service when capability mapping is absent.
 */
public class PolicyEvaluationClient {

    private static final Logger logger = LoggerFactory.getLogger(PolicyEvaluationClient.class);

    private final RestTemplate restTemplate;
    private final SecurityProperties.DynamicRbacProperties properties;

    public PolicyEvaluationClient(RestTemplate restTemplate,
                                  SecurityProperties.DynamicRbacProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Optional<Boolean> evaluate(Long endpointId, Set<String> roles) {
        if (endpointId == null || roles == null) {
            logger.warn("Policy evaluation called with missing endpoint or roles");
            return Optional.empty();
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(resolveEvaluationUri()).build().toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(properties.getApiKey())) {
                headers.set(properties.getApiKeyHeader(), properties.getApiKey());
            }

            PolicyEvaluationRequest request = new PolicyEvaluationRequest(endpointId, roles);

            ResponseEntity<PolicyEvaluationResponse> response =
                restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers),
                    PolicyEvaluationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody().isAllowed());
            }

            logger.warn("Policy evaluation returned status {} for endpoint {}",
                response.getStatusCode(), endpointId);
        } catch (RestClientException ex) {
            logger.error("Policy evaluation failed for endpoint {}: {}", endpointId, ex.getMessage());
        }

        return Optional.empty();
    }

    private String resolveEvaluationUri() {
        return resolveUrl(properties.getPolicyEvaluationPath());
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
}
