package com.shared.utilities.fileupload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the {@link FileStorageService}.
 */
@Configuration
@ConditionalOnClass(FileStorageService.class)
@EnableConfigurationProperties(FileStorageProperties.class)
@ConditionalOnProperty(prefix = "shared-lib.file-upload", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService(FileStorageProperties properties) {
        return new FileStorageService(properties.getBaseDir());
    }
}
