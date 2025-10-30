package com.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Generic property loader for shared-lib utilities.
 * All properties are prefixed with "shared-lib".
 */
@ConfigurationProperties(prefix = "shared-lib")
public class SharedLibConfigurationProperties {

    private final AuditProperties audit = new AuditProperties();
    private final EntityAuditProperties entityAudit = new EntityAuditProperties();
    private final SftpProperties sftp = new SftpProperties();
    private final SecurityProperties security = new SecurityProperties();

    public AuditProperties getAudit() {
        return audit;
    }

    public EntityAuditProperties getEntityAudit() {
        return entityAudit;
    }

    public SftpProperties getSftp() {
        return sftp;
    }

    public SecurityProperties getSecurity() {
        return security;
    }
}
