package com.shared.config;

/**
 * Configuration properties for the security utility.
 */
public class SecurityProperties {

    private boolean enabled = false;
    private String[] permittedPaths = {"/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};

    // Getters and setters
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
}
