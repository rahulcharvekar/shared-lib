package com.shared.security.rls;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * RLSContextManager manages Row-Level Security (RLS) context for the current request.
 * 
 * This component sets the current user ID as a transaction-local setting in PostgreSQL,
 * enabling RLS policies to filter rows based on the user's allowed (board_id, employer_id) pairs.
 * 
 * Usage:
 *   After JWT verification in a request:
 *   RLSContextManager.setContext(userId);
 *   
 *   All subsequent database queries in that transaction will be automatically filtered by RLS policies.
 * 
 * Important:
 *   - Must be called once per HTTP request, before any database operations
 *   - Context persists for the duration of the transaction/request
 *   - Context is automatically cleared when the transaction commits/rolls back
 *   - Works with Spring @Transactional and HikariCP connection pooling
 */
@Slf4j
@Component
public class RLSContextManager {

    private static final String SET_CONTEXT_FUNCTION = "SELECT auth.set_user_context(?)";
    private static final String GET_CONTEXT_FUNCTION = "SELECT auth.get_user_context()";
    private static final String CLEAR_CONTEXT_FUNCTION = "SELECT auth.clear_user_context()";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Sets the RLS context for the current user in the current transaction.
     * 
     * This function must be called early in request processing, typically in a filter
     * or interceptor after JWT verification.
     * 
     * @param userId the user ID to set in the RLS context
     * @throws IllegalArgumentException if userId is null or empty
     * @throws Exception if the database call fails
     */
    public void setContext(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        try {
            log.debug("Setting RLS context for user: {}", userId);
            jdbcTemplate.queryForObject(SET_CONTEXT_FUNCTION, new Object[]{userId}, String.class);
            log.debug("RLS context set successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to set RLS context for user: {}", userId, e);
            throw new RuntimeException("Failed to set RLS context: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the current RLS context user ID.
     * 
     * This is useful for auditing or debugging to verify which user's context is active.
     * 
     * @return the current user ID in the RLS context, or null if not set
     */
    public String getContext() {
        try {
            String userId = jdbcTemplate.queryForObject(GET_CONTEXT_FUNCTION, String.class);
            log.debug("Retrieved RLS context: {}", userId);
            return userId;
        } catch (Exception e) {
            log.warn("Failed to retrieve RLS context", e);
            return null;
        }
    }

    /**
     * Explicitly clears the RLS context for the current transaction.
     * 
     * This is optional; context is automatically cleared when the transaction ends.
     * Use this if you need to explicitly reset context within a long-running transaction.
     * 
     * @throws Exception if the database call fails
     */
    public void clearContext() {
        try {
            log.debug("Clearing RLS context");
            jdbcTemplate.queryForObject(CLEAR_CONTEXT_FUNCTION, String.class);
            log.debug("RLS context cleared successfully");
        } catch (Exception e) {
            log.warn("Failed to clear RLS context", e);
            // Don't throw; clearing is best-effort
        }
    }

    /**
     * Convenience method to set context and execute a callback.
     * 
     * This is useful for ad-hoc operations that need a specific user context.
     * 
     * @param userId the user ID to set
     * @param callback the operation to execute with this context
     * @throws Exception if the callback throws
     */
    public <T> T withContext(String userId, ContextCallback<T> callback) throws Exception {
        setContext(userId);
        try {
            return callback.execute();
        } finally {
            clearContext();
        }
    }

    /**
     * Functional interface for executing code with a specific RLS context.
     */
    @FunctionalInterface
    public interface ContextCallback<T> {
        T execute() throws Exception;
    }
}
