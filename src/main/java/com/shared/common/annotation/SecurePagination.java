package com.shared.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark pagination endpoints as requiring secure date range filtering.
 * This ensures all paginated APIs enforce mandatory startDate and endDate parameters.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurePagination {
    
    /**
     * Whether date range filtering is mandatory (default: true)
     */
    boolean requireDateRange() default true;
    
    /**
     * Maximum allowed page size (default: 100)
     */
    int maxPageSize() default 100;
    
    /**
     * Default page size (default: 20)
     */
    int defaultPageSize() default 20;
    
    /**
     * Whether to generate opaque pagination tokens (default: true)
     */
    boolean opaqueTokens() default true;
    
    /**
     * Custom error message for missing date range
     */
    String dateRangeErrorMessage() default "Start date and end date are mandatory for secure pagination";
}
