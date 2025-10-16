package com.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Core auto-configuration for shared library properties.
 * Registers SharedLibConfigurationProperties bean unconditionally.
 */
@Configuration
@EnableConfigurationProperties(SharedLibConfigurationProperties.class)
public class SharedLibCoreAutoConfiguration {
}
