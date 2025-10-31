package com.shared.security;

import com.shared.security.client.TokenIntrospectionClient;
import com.shared.security.client.TokenIntrospectionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtConfig jwtConfig;
    private final TokenIntrospectionClient tokenIntrospectionClient;

    public JwtAuthenticationFilter(JwtConfig jwtConfig, TokenIntrospectionClient tokenIntrospectionClient) {
        this.jwtConfig = jwtConfig;
        this.tokenIntrospectionClient = tokenIntrospectionClient;
    }

    @jakarta.annotation.PostConstruct
    public void logSecretLength() {
        log.info("JwtAuthenticationFilter bean created. (jwtSecret length: {}, jwtIssuer: {}, jwtAudience: {})",
            jwtConfig.getSecret() != null ? jwtConfig.getSecret().length() : "null",
            jwtConfig.getIssuer(),
            jwtConfig.getAudience());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter: doFilterInternal called for URI: {}", request.getRequestURI());
        String jwt = getJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!validateToken(jwt)) {
            log.debug("JWT validation failed for request URI: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        if (tokenIntrospectionClient != null) {
            TokenIntrospectionClient.TokenIntrospectionResult result = tokenIntrospectionClient.introspect(jwt);
            switch (result.getStatus()) {
                case ACTIVE -> {
                    TokenIntrospectionResponse details = result.getResponse().orElseThrow();
                    String principal = details.getSubject();
                    if (!StringUtils.hasText(principal)) {
                        principal = getClaims(jwt).getSubject();
                    }
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                    authentication.setDetails(new JwtAuthenticationDetails(request, details));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                case INACTIVE -> {
                    log.debug("Token introspection rejected request for URI: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inactive");
                    return;
                }
                case ERROR -> {
                    if (result.isAllowOnError()) {
                        log.warn("Token introspection unavailable for URI: {}. Falling back to local JWT validation (fail-open enabled).",
                                request.getRequestURI());
                        authenticateWithClaims(request, jwt);
                    } else {
                        log.error("Token introspection unavailable for URI: {}. Denying request.", request.getRequestURI());
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Token introspection unavailable");
                        return;
                    }
                }
            }
        } else {
            authenticateWithClaims(request, jwt);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void authenticateWithClaims(HttpServletRequest request, String jwt) {
        Claims claims = getClaims(jwt);
        String username = claims.getSubject();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception ex) {
            log.debug("JWT validation error: {}", ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSecretSigningKey())
            .requireIssuer(jwtConfig.getIssuer())
            .requireAudience(jwtConfig.getAudience())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private static javax.crypto.SecretKey getSecretSigningKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        try {
            // Try hex decoding first
            byte[] keyBytes = hexStringToByteArray(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            // Fallback to base64 decoding
            try {
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception ex) {
                // Use string directly if both fail
                return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private javax.crypto.SecretKey getSecretSigningKey() {
        return getSecretSigningKey(jwtConfig.getSecret());
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
