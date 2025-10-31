package com.shared.security.rbac.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable snapshot representing authorization state for a user.
 */
public final class AuthorizationMatrix {

    private final Long userId;
    private final Integer permissionVersion;
    private final Set<String> roles;
    private final Set<String> capabilities;

    @JsonCreator
    public AuthorizationMatrix(
            @JsonProperty("userId") Long userId,
            @JsonProperty("permissionVersion") Integer permissionVersion,
            @JsonProperty("roles") Set<String> roles,
            @JsonProperty("capabilities") Set<String> capabilities) {
        this.userId = userId;
        this.permissionVersion = permissionVersion;
        this.roles = roles != null ? Collections.unmodifiableSet(new HashSet<>(roles)) : Set.of();
        this.capabilities = capabilities != null ? Collections.unmodifiableSet(new HashSet<>(capabilities)) : Set.of();
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getPermissionVersion() {
        return permissionVersion;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getCapabilities() {
        return capabilities;
    }

    @Override
    public String toString() {
        return "AuthorizationMatrix{" +
            "userId=" + userId +
            ", permissionVersion=" + permissionVersion +
            ", roles=" + roles +
            ", capabilities=" + capabilities +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorizationMatrix that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
            && Objects.equals(permissionVersion, that.permissionVersion)
            && Objects.equals(roles, that.roles)
            && Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, permissionVersion, roles, capabilities);
    }
}
