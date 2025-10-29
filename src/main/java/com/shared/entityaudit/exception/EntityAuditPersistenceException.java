package com.shared.entityaudit.exception;

/**
 * Exception thrown when entity audit persistence fails.
 */
public class EntityAuditPersistenceException extends RuntimeException {

    public EntityAuditPersistenceException(String message) {
        super(message);
    }

    public EntityAuditPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
