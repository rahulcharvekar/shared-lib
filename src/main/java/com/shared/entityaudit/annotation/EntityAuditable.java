package com.shared.entityaudit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic entity-level auditing.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityAuditable {

    /**
     * Logical entity type (e.g., WORKER_RECEIPT). Supports plain strings.
     */
    String entityType();

    /**
     * Expression resolving to the entity's record number (one per entity instance).
     * Leave blank to let the library generate and persist a record number automatically.
     */
    String recordNumber() default "";

    /**
     * Expression resolving to the entity's identifier (optional).
     */
    String entityId() default "";

    /**
     * CRUD operation being performed (CREATE, UPDATE, DELETE, etc.).
     */
    String operation();

    /**
     * Expression resolving to the audit number if caller wants to supply one; leave blank to auto-generate.
     */
    String auditNumber() default "";

    /**
     * Expression resolving to a human-readable change summary.
     */
    String changeSummary() default "";

    /**
     * Expression resolving to a Map representing old values.
     */
    String oldValues() default "";

    /**
     * Expression resolving to a Map representing new values.
     */
    String newValues() default "";

    /**
     * Expression resolving to a Map containing additional metadata keys.
     */
    String metadata() default "";
}
