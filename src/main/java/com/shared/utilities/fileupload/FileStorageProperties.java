package com.shared.utilities.fileupload;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for shared file storage utility.
 */
@ConfigurationProperties(prefix = "shared-lib.file-upload")
public class FileStorageProperties {

    /**
     * Base directory where files will be stored. Defaults to "uploads".
     */
    private String baseDir = "uploads";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
