package com.shared.common.config;

import com.shared.common.filter.RequestIdFilter;
import com.shared.common.filter.SignatureVerificationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for common filters.
 * Enables filters conditionally to avoid forcing consumers into component scanning.
 */
@Configuration
@ConditionalOnProperty(prefix = "shared-lib.filters", name = "enabled", havingValue = "true")
public class FiltersAutoConfiguration {

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    @Bean
    public SignatureVerificationFilter signatureVerificationFilter() {
        return new SignatureVerificationFilter();
    }
}
