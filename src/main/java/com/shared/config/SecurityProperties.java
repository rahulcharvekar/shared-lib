package com.shared.config;

/**
 * Configuration properties for the security utility.
 */
public class SecurityProperties {

    private boolean enabled = false;
    private String[] permittedPaths = {"/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
    private final IntrospectionProperties introspection = new IntrospectionProperties();
    private final DynamicRbacProperties dynamicRbac = new DynamicRbacProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getPermittedPaths() {
        return permittedPaths;
    }

    public void setPermittedPaths(String[] permittedPaths) {
        this.permittedPaths = permittedPaths;
    }

    public IntrospectionProperties getIntrospection() {
        return introspection;
    }

    public DynamicRbacProperties getDynamicRbac() {
        return dynamicRbac;
    }

    public static class IntrospectionProperties {
        private boolean enabled = true;
        private String url;
        private String apiKeyHeader = "X-Internal-Api-Key";
        private String apiKey;
        private java.time.Duration connectTimeout = java.time.Duration.ofSeconds(2);
        private java.time.Duration readTimeout = java.time.Duration.ofSeconds(2);
        private boolean failOpen = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public java.time.Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(java.time.Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public java.time.Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(java.time.Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public boolean isFailOpen() {
            return failOpen;
        }

        public void setFailOpen(boolean failOpen) {
            this.failOpen = failOpen;
        }
    }

    public static class DynamicRbacProperties {
        private boolean enabled = false;
        private String baseUrl;
        private String authorizationMatrixPath = "/internal/authz/users/{userId}/matrix";
        private String endpointMetadataPath = "/internal/authz/endpoints/metadata";
        private String policyEvaluationPath = "/internal/authz/policies/evaluate";
        private boolean policyEvaluationEnabled = true;
        private boolean forwardAuthorizationHeader = true;
        private String apiKeyHeader = "X-Internal-Api-Key";
        private String apiKey;
        private java.time.Duration matrixCacheTtl = java.time.Duration.ofSeconds(5);
        private java.time.Duration metadataCacheTtl = java.time.Duration.ofSeconds(30);
        private java.time.Duration connectTimeout = java.time.Duration.ofSeconds(2);
        private java.time.Duration readTimeout = java.time.Duration.ofSeconds(2);
        private boolean failOpen = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getAuthorizationMatrixPath() {
            return authorizationMatrixPath;
        }

        public void setAuthorizationMatrixPath(String authorizationMatrixPath) {
            this.authorizationMatrixPath = authorizationMatrixPath;
        }

        public String getEndpointMetadataPath() {
            return endpointMetadataPath;
        }

        public void setEndpointMetadataPath(String endpointMetadataPath) {
            this.endpointMetadataPath = endpointMetadataPath;
        }

        public String getPolicyEvaluationPath() {
            return policyEvaluationPath;
        }

        public void setPolicyEvaluationPath(String policyEvaluationPath) {
            this.policyEvaluationPath = policyEvaluationPath;
        }

        public boolean isPolicyEvaluationEnabled() {
            return policyEvaluationEnabled;
        }

        public void setPolicyEvaluationEnabled(boolean policyEvaluationEnabled) {
            this.policyEvaluationEnabled = policyEvaluationEnabled;
        }

        public boolean isForwardAuthorizationHeader() {
            return forwardAuthorizationHeader;
        }

        public void setForwardAuthorizationHeader(boolean forwardAuthorizationHeader) {
            this.forwardAuthorizationHeader = forwardAuthorizationHeader;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public java.time.Duration getMatrixCacheTtl() {
            return matrixCacheTtl;
        }

        public void setMatrixCacheTtl(java.time.Duration matrixCacheTtl) {
            this.matrixCacheTtl = matrixCacheTtl;
        }

        public java.time.Duration getMetadataCacheTtl() {
            return metadataCacheTtl;
        }

        public void setMetadataCacheTtl(java.time.Duration metadataCacheTtl) {
            this.metadataCacheTtl = metadataCacheTtl;
        }

        public java.time.Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(java.time.Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public java.time.Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(java.time.Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public boolean isFailOpen() {
            return failOpen;
        }

        public void setFailOpen(boolean failOpen) {
            this.failOpen = failOpen;
        }
    }
}
