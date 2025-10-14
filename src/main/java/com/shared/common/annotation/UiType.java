package com.shared.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the UI type for an endpoint
 * This helps the frontend understand how to render and interact with the endpoint
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UiType {

    /**
     * The UI type for this endpoint
     * @return the UI type string
     */
    String value();

    /**
     * Optional description of how this endpoint should be used in the UI
     * @return usage description
     */
    String usage() default "";
}
