package com.shared.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic auditing.
 * When a method is annotated with @Auditable, an audit event will be recorded
 * automatically upon method execution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action performed by the method.
     */
    String action();

    /**
     * The type of resource being acted upon.
     */
    String resourceType();

    /**
     * The ID of the resource, can be a SpEL expression.
     * For example: "#id" or "#user.id"
     */
    String resourceId() default "";

    /**
     * Additional details as a map, can be SpEL expressions.
     * For example: "{'key': 'value', 'param': #param}"
     */
    String details() default "";

    /**
     * Old values before the operation, as a map, can be SpEL expressions.
     * For example: "{'name': #oldEntity.name, 'status': #oldEntity.status}"
     */
    String oldValues() default "";

    /**
     * New values after the operation, as a map, can be SpEL expressions.
     * For example: "{'name': #newEntity.name, 'status': #newEntity.status}"
     */
    String newValues() default "";
}
