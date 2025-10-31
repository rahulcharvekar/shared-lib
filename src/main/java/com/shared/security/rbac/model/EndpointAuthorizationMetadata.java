package com.shared.security.rbac.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Metadata describing authorization requirements for a cataloged endpoint.
 */
public final class EndpointAuthorizationMetadata {

    private final boolean endpointFound;
    private final Long endpointId;
    private final boolean hasPolicies;
    private final Set<Long> policyIds;
    private final Set<String> requiredCapabilities;

    @JsonCreator
    public EndpointAuthorizationMetadata(
            @JsonProperty("endpointFound") boolean endpointFound,
            @JsonProperty("endpointId") Long endpointId,
            @JsonProperty("hasPolicies") boolean hasPolicies,
            @JsonProperty("policyIds") Set<Long> policyIds,
            @JsonProperty("requiredCapabilities") Set<String> requiredCapabilities) {
        this.endpointFound = endpointFound;
        this.endpointId = endpointId;
        this.hasPolicies = hasPolicies;
        this.policyIds = policyIds != null ? Collections.unmodifiableSet(new HashSet<>(policyIds)) : Set.of();
        this.requiredCapabilities = requiredCapabilities != null
                ? Collections.unmodifiableSet(new HashSet<>(requiredCapabilities))
                : Set.of();
    }

    public boolean isEndpointFound() {
        return endpointFound;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public boolean hasPolicies() {
        return hasPolicies;
    }

    public Set<Long> getPolicyIds() {
        return policyIds;
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    @Override
    public String toString() {
        return "EndpointAuthorizationMetadata{" +
            "endpointFound=" + endpointFound +
            ", endpointId=" + endpointId +
            ", hasPolicies=" + hasPolicies +
            ", policyIds=" + policyIds +
            ", requiredCapabilities=" + requiredCapabilities +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointAuthorizationMetadata that)) {
            return false;
        }
        return endpointFound == that.endpointFound
            && hasPolicies == that.hasPolicies
            && Objects.equals(endpointId, that.endpointId)
            && Objects.equals(policyIds, that.policyIds)
            && Objects.equals(requiredCapabilities, that.requiredCapabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointFound, endpointId, hasPolicies, policyIds, requiredCapabilities);
    }
}
