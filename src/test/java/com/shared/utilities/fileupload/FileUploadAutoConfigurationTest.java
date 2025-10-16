package com.shared.utilities.fileupload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FileUploadAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FileUploadAutoConfiguration.class));

    @Test
    void whenEnabled_fileStorageServiceBeanCreated() {
        contextRunner
            .withPropertyValues("shared-lib.file-upload.enabled=true", "shared-lib.file-upload.base-dir=/tmp/uploads")
            .run(context -> {
                assertThat(context).hasSingleBean(FileStorageService.class);
                FileStorageService service = context.getBean(FileStorageService.class);
                FileStorageProperties props = context.getBean(FileStorageProperties.class);
                assertThat(props.getBaseDir()).isEqualTo("/tmp/uploads");
                assertThat(service.getBaseUploadDir()).isEqualTo("/tmp/uploads");
            });
    }

    @Test
    void whenDisabled_noBeanCreated() {
        contextRunner
            .withPropertyValues("shared-lib.file-upload.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(FileStorageService.class));
    }
}
