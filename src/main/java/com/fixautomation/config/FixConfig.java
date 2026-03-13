package com.fixautomation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * FIX engine configuration properties.
 * Reads from fix-config.yaml via YAML property source.
 */
@Configuration
@ConfigurationProperties(prefix = "fix")
public class FixConfig {

    /** Whether the FIX client is enabled (set false for non-FIX test runs). */
    private boolean enabled = false;

    /** Path to the QuickFIX/J session config file on the classpath. */
    private String configFile = "fixmessages/quickfix.cfg";

    /** Default timeout in seconds to wait for an ExecutionReport. */
    private long responseTimeoutSeconds = 30;

    /** Interval in milliseconds between polls when waiting for a response. */
    private long pollIntervalMillis = 500;

    /** FIX sender comp ID. */
    private String senderCompId = "CLIENT";

    /** FIX target comp ID. */
    private String targetCompId = "SERVER";

    /** FIX engine host. */
    private String host = "localhost";

    /** FIX engine port. */
    private int port = 9878;

    /** FIX protocol version (e.g. FIX.4.4). */
    private String beginString = "FIX.4.4";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public long getResponseTimeoutSeconds() {
        return responseTimeoutSeconds;
    }

    public void setResponseTimeoutSeconds(long responseTimeoutSeconds) {
        this.responseTimeoutSeconds = responseTimeoutSeconds;
    }

    public long getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void setPollIntervalMillis(long pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public String getSenderCompId() {
        return senderCompId;
    }

    public void setSenderCompId(String senderCompId) {
        this.senderCompId = senderCompId;
    }

    public String getTargetCompId() {
        return targetCompId;
    }

    public void setTargetCompId(String targetCompId) {
        this.targetCompId = targetCompId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBeginString() {
        return beginString;
    }

    public void setBeginString(String beginString) {
        this.beginString = beginString;
    }
}
