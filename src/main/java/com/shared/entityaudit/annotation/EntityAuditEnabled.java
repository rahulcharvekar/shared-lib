package com.shared.entityaudit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.EntityListeners;

import com.shared.entityaudit.listener.SharedEntityAuditListener;

/**
 * Opt-in marker to enable automatic entity auditing via JPA lifecycle events.
 * Apply this to JPA entities that also implement {@code EntityAuditDescriptor}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EntityListeners(SharedEntityAuditListener.class)
public @interface EntityAuditEnabled {
}

