package com.shared.security.rbac;

import com.shared.config.SecurityProperties;
import com.shared.security.JwtAuthenticationDetails;
import com.shared.security.rbac.client.AuthorizationMatrixClient;
import com.shared.security.rbac.client.EndpointAuthorizationMetadataClient;
import com.shared.security.rbac.client.PolicyEvaluationClient;
import com.shared.security.rbac.model.AuthorizationMatrix;
import com.shared.security.rbac.model.EndpointAuthorizationMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * AuthorizationManager that enforces RBAC policies resolved from auth-service catalog.
 */
public class DynamicEndpointAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DynamicEndpointAuthorizationManager.class);

    private final AuthorizationMatrixClient matrixClient;
    private final EndpointAuthorizationMetadataClient metadataClient;
    private final PolicyEvaluationClient policyEvaluationClient;
    private final SecurityProperties.DynamicRbacProperties properties;

    public DynamicEndpointAuthorizationManager(AuthorizationMatrixClient matrixClient,
                                               EndpointAuthorizationMetadataClient metadataClient,
                                               PolicyEvaluationClient policyEvaluationClient,
                                               SecurityProperties.DynamicRbacProperties properties) {
        this.matrixClient = matrixClient;
        this.metadataClient = metadataClient;
        this.policyEvaluationClient = policyEvaluationClient;
        this.properties = properties;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                       RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return new AuthorizationDecision(true);
        }

        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Dynamic RBAC denied {} {} due to missing authentication", method, path);
            return new AuthorizationDecision(false);
        }

        Long userId = extractUserId(authentication);
        Integer permissionVersion = extractPermissionVersion(authentication);

        if (userId == null) {
            logger.warn("Dynamic RBAC cannot resolve user id for {} {}; denying access", method, path);
            return new AuthorizationDecision(false);
        }

        Optional<AuthorizationMatrix> matrixOpt =
            matrixClient.fetch(userId, permissionVersion, request);
        if (matrixOpt.isEmpty()) {
            return decisionOnError("Unable to load authorization matrix for user " + userId);
        }
        AuthorizationMatrix matrix = matrixOpt.get();

        Optional<EndpointAuthorizationMetadata> metadataOpt =
            metadataClient.fetch(method, path);
        if (metadataOpt.isEmpty()) {
            return decisionOnError("Unable to load endpoint metadata for " + method + " " + path);
        }
        EndpointAuthorizationMetadata metadata = metadataOpt.get();

        if (!metadata.isEndpointFound()) {
            logger.warn("Endpoint {} {} not cataloged; denying access", method, path);
            return new AuthorizationDecision(false);
        }

        if (!metadata.hasPolicies()) {
            logger.warn("Endpoint {} {} has no policies assigned; denying access", method, path);
            return new AuthorizationDecision(false);
        }

        Set<String> requiredCapabilities = metadata.getRequiredCapabilities();
        boolean allowed;
        if (!requiredCapabilities.isEmpty()) {
            allowed = requiredCapabilities.stream().anyMatch(matrix.getCapabilities()::contains);
            if (!allowed) {
                logger.debug("Denied {} {} for user {} - missing capabilities {} (has {})",
                    method, path, userId, requiredCapabilities, matrix.getCapabilities());
            }
        } else if (properties.isPolicyEvaluationEnabled() && policyEvaluationClient != null) {
            Optional<Boolean> decision = policyEvaluationClient.evaluate(metadata.getEndpointId(), matrix.getRoles());
            if (decision.isEmpty()) {
                return decisionOnError("Policy evaluation failed for endpoint " + metadata.getEndpointId());
            }
            allowed = decision.get();
            if (!allowed) {
                logger.debug("Denied {} {} for user {} - policy evaluation negative (roles: {})",
                    method, path, userId, matrix.getRoles());
            }
        } else {
            logger.warn("Endpoint {} {} has policies but no capability mapping and policy evaluation disabled; denying",
                method, path);
            return new AuthorizationDecision(false);
        }

        if (allowed) {
            logger.trace("Authorized {} {} for user {}", method, path, userId);
        }
        return new AuthorizationDecision(allowed);
    }

    private AuthorizationDecision decisionOnError(String message) {
        if (properties.isFailOpen()) {
            logger.error("{}; allowing request because fail-open is enabled", message);
            return new AuthorizationDecision(true);
        }
        logger.error("{}; denying request", message);
        return new AuthorizationDecision(false);
    }

    private Long extractUserId(Authentication authentication) {

        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            Long userId = jwtDetails.getUserId();
            if (userId != null) {
                return userId;
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long longId) {
            return longId;
        }
        if (principal instanceof Number number) {
            return number.longValue();
        }
        if (principal instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        if (principal != null) {
            try {
                var method = principal.getClass().getMethod("getId");
                Object result = method.invoke(principal);
                if (result instanceof Number numeric) {
                    return numeric.longValue();
                }
            } catch (ReflectiveOperationException ignored) {
                // ignore
            }
        }
        return null;
    }

    private Integer extractPermissionVersion(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            return jwtDetails.getPermissionVersion();
        }
        return null;
    }
}
