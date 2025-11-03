package com.shared.security.rls;

import com.shared.security.JwtAuthenticationDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * RLSContextFilter automatically sets the RLS context for each HTTP request.
 * 
 * This filter runs after Spring Security authentication and extracts the authenticated user's ID
 * to set in the RLS context. This ensures all database queries in the request are properly
 * filtered by RLS policies based on the user's allowed tenants.
 * 
 * Integration:
 *   Register this filter in your Spring Security configuration:
 *   
 *   @Bean
 *   public RLSContextFilter rlsContextFilter() {
 *       return new RLSContextFilter();
 *   }
 *   
 *   // In SecurityFilterChain:
 *   http.addFilterAfter(rlsContextFilter(), SecurityContextHolder.class);
 * 
 * Lifecycle:
 *   1. Request arrives
 *   2. Spring Security processes authentication
 *   3. This filter extracts the authenticated user ID
 *   4. RLSContextManager.setContext(userId) is called
 *   5. Request proceeds; all DB queries are RLS-filtered
 *   6. Response returns; transaction ends and context is automatically cleared
 */
@Slf4j
@Component
public class RLSContextFilter extends OncePerRequestFilter {

    @Autowired
    private RLSContextManager rlsContextManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Extract authenticated user ID from Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                String userId = extractUserId(principal);
                
                if (userId != null && !userId.isEmpty()) {
                    log.debug("Setting RLS context for request: user={}, path={}", userId, request.getRequestURI());
                    rlsContextManager.setContext(userId);
                } else {
                    log.warn("No user ID found in authentication for request: {}", request.getRequestURI());
                }
            } else {
                log.debug("No authentication found for request: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Error setting RLS context", e);
            // Don't fail the request if context setting fails; let it proceed
            // (the database will reject unpermitted queries anyway if RLS is enforced)
        }
        
        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the user ID from the authentication principal.
     * Supports multiple principal types for flexibility across services.
     * 
     * @param principal the authentication principal
     * @return the user ID as a String, or null if not extractable
     */
    private String extractUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        
        // PRIORITY 1: Check authentication details for user_id from JWT introspection
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof JwtAuthenticationDetails) {
            JwtAuthenticationDetails details = (JwtAuthenticationDetails) auth.getDetails();
            Long userId = details.getUserId();
            if (userId != null) {
                log.trace("Extracted user ID from JwtAuthenticationDetails: {}", userId);
                return userId.toString();
            }
        }
        
        // PRIORITY 2: Try reflection to get getId() method (works with User entities that have getId())
        try {
            java.lang.reflect.Method getIdMethod = principal.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(principal);
            if (id != null) {
                log.trace("Extracted user ID via getId() method: {}", id);
                return id.toString();
            }
        } catch (Exception e) {
            // getId() method not found or failed, try other methods
            log.trace("Could not extract user ID via getId() method", e);
        }
        
        // PRIORITY 3: If principal is a UserDetails, try to use username
        // NOTE: This returns username, not user_id, which may not work for RLS
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) principal;
            log.warn("Falling back to username as user ID (may not work with RLS): {}", userDetails.getUsername());
            return userDetails.getUsername();
        }
        
        // PRIORITY 4: Last resort - use toString() (username)
        log.warn("Falling back to principal.toString() as user ID (may not work with RLS): {}", principal);
        return principal.toString();
    }
}
