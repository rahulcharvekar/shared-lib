package com.shared.utilities.sftp;

import com.shared.config.SharedLibConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for exposing {@link SftpUtil} when the feature is enabled.
 */
@Configuration
@ConditionalOnClass(SftpUtil.class)
@ConditionalOnProperty(prefix = "shared-lib.sftp", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SharedLibConfigurationProperties.class)
public class SftpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SftpUtil sftpUtil(SharedLibConfigurationProperties sharedLibProperties) {
        return new SftpUtil(sharedLibProperties);
    }
}
