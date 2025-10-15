package com.shared.audit;

import com.shared.audit.config.AuditAutoConfiguration;
import com.shared.audit.service.AuditTrailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuditAutoConfigurationDisabledTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test
    void whenAuditDisabled_noBeansAreRegistered() {
        contextRunner
                .withPropertyValues("audit.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AuditTrailService.class));
    }
}
