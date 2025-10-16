package com.shared.utilities.sftp;

import com.shared.config.SharedLibCoreAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SftpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SharedLibCoreAutoConfiguration.class, SftpAutoConfiguration.class));

    @Test
    void whenSftpEnabled_sftpUtilBeanIsCreated() {
        contextRunner
            .withPropertyValues("shared-lib.sftp.enabled=true")
            .run(context -> assertThat(context).hasSingleBean(SftpUtil.class));
    }

    @Test
    void whenSftpDisabled_sftpUtilBeanIsNotCreated() {
        contextRunner
            .withPropertyValues("shared-lib.sftp.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SftpUtil.class));
    }
}
