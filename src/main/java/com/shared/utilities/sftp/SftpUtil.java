package com.shared.utilities.sftp;

import com.jcraft.jsch.*;
import com.shared.config.SftpProperties;
import com.shared.config.SharedLibConfigurationProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Utility class for SFTP operations.
 */
public class SftpUtil {

    private final SftpProperties properties;
    private Session session;
    private ChannelSftp channelSftp;

    public SftpUtil(SharedLibConfigurationProperties sharedLibProperties) {
        this.properties = sharedLibProperties.getSftp();
    }

    /**
     * Connect to the SFTP server.
     */
    public void connect() throws JSchException {
        JSch jsch = new JSch();
        if (properties.getKnownHostsFile() != null) {
            jsch.setKnownHosts(properties.getKnownHostsFile());
        }

        session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
        session.setPassword(properties.getPassword());

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", properties.isStrictHostKeyChecking() ? "yes" : "no");
        session.setConfig(config);

        session.connect();
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
    }

    /**
     * Disconnect from the SFTP server.
     */
    public void disconnect() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * List files in the remote directory.
     */
    @SuppressWarnings("unchecked")
    public List<String> listFiles() throws SftpException {
        Vector<ChannelSftp.LsEntry> files = channelSftp.ls(properties.getRemoteDirectory());
        List<String> fileNames = new ArrayList<>();
        for (ChannelSftp.LsEntry file : files) {
            if (!file.getAttrs().isDir()) {
                fileNames.add(file.getFilename());
            }
        }
        return fileNames;
    }

    /**
     * Download a file from remote directory to local directory.
     */
    public void downloadFile(String remoteFileName, String localFileName) throws SftpException, IOException {
        String remotePath = properties.getRemoteDirectory() + "/" + remoteFileName;
        String localPath = properties.getLocalDirectory() + "/" + localFileName;

        // Ensure local directory exists
        File localDir = new File(properties.getLocalDirectory());
        if (!localDir.exists()) {
            localDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(localPath)) {
            channelSftp.get(remotePath, fos);
        }
    }

    /**
     * Move a file on the remote server (e.g., to a processed directory).
     */
    public void moveRemoteFile(String fileName, String newDirectory) throws SftpException {
        String from = properties.getRemoteDirectory() + "/" + fileName;
        String to = newDirectory + "/" + fileName;
        channelSftp.rename(from, to);
    }

    /**
     * Delete a file on the remote server.
     */
    public void deleteRemoteFile(String fileName) throws SftpException {
        String path = properties.getRemoteDirectory() + "/" + fileName;
        channelSftp.rm(path);
    }
}
