package com.fixautomation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${websocket.url}")
    private String websocketUrl;

    @Value("${rabbitmq.management.host}")
    private String rabbitmqManagementHost;

    @Value("${rabbitmq.management.username}")
    private String rabbitmqUsername;

    @Value("${rabbitmq.management.password}")
    private String rabbitmqPassword;

    @Value("${rabbitmq.management.vhost:/}")
    private String rabbitmqVhost;

    @Value("${rabbitmq.request.queue}")
    private String requestQueue;

    @Value("${rabbitmq.response.queue}")
    private String responseQueue;

    @Value("${polling.timeout.seconds:30}")
    private long pollingTimeoutSeconds;

    @Value("${polling.interval.millis:500}")
    private long pollingIntervalMillis;

    @Value("${websocket.ack.timeout.seconds:10}")
    private long websocketAckTimeoutSeconds;

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public String getRabbitmqManagementHost() {
        return rabbitmqManagementHost;
    }

    public String getRabbitmqUsername() {
        return rabbitmqUsername;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public String getRabbitmqVhost() {
        return rabbitmqVhost;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }

    public long getPollingTimeoutSeconds() {
        return pollingTimeoutSeconds;
    }

    public long getPollingIntervalMillis() {
        return pollingIntervalMillis;
    }

    public long getWebsocketAckTimeoutSeconds() {
        return websocketAckTimeoutSeconds;
    }
}
