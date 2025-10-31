package com.shared.security.rbac.model;

/**
 * Response payload for policy evaluation call.
 */
public class PolicyEvaluationResponse {

    private boolean allowed;

    public PolicyEvaluationResponse() {
    }

    public PolicyEvaluationResponse(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}
