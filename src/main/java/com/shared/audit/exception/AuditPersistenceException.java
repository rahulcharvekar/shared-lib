package com.shared.audit.exception;

/**
 * Raised when the audit event fails to persist to the database.
 */
public class AuditPersistenceException extends RuntimeException {

    public AuditPersistenceException(String message) {
        super(message);
    }

    public AuditPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
