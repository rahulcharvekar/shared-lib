package com.shared.security.rbac.model;

import java.util.Set;

/**
 * Request payload for policy evaluation in auth-service.
 */
public class PolicyEvaluationRequest {

    private Long endpointId;
    private Set<String> roles;

    public PolicyEvaluationRequest() {
    }

    public PolicyEvaluationRequest(Long endpointId, Set<String> roles) {
        this.endpointId = endpointId;
        this.roles = roles;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
