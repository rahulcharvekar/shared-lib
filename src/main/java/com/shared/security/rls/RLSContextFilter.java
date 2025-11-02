package com.shared.security.rls;

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
                // Get the principal (user ID/name)
                String userId = authentication.getName();
                
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
}
