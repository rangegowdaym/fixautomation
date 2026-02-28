package com.fixautomation.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixautomation.config.AppConfig;
import com.fixautomation.model.OrderResponse;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Management API client using HTTP (NOT AMQP).
 * Provides publish and getMessage operations with Basic Auth.
 */
@Component
public class RabbitMQManagementService {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQManagementService.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AppConfig appConfig;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitMQManagementService(AppConfig appConfig, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a message to a queue via the RabbitMQ Management HTTP API.
     *
     * @param queueName   the target queue
     * @param payload     the JSON payload as a string
     * @param correlationId the correlationId to include in message properties
     */
    public void publish(String queueName, String payload, String correlationId) throws IOException {
        String vhost = encodeVhost(appConfig.getRabbitmqVhost());
        String url = String.format("%s/api/exchanges/%s/amq.default/publish",
                appConfig.getRabbitmqManagementHost(), vhost);

        Map<String, Object> properties = new HashMap<>();
        properties.put("correlation_id", correlationId);
        properties.put("content_type", "application/json");
        properties.put("delivery_mode", 2);

        Map<String, Object> body = new HashMap<>();
        body.put("properties", properties);
        body.put("routing_key", queueName);
        body.put("payload", payload);
        body.put("payload_encoding", "string");

        String requestBody = objectMapper.writeValueAsString(body);
        log.info("Publishing to queue '{}' via Management API: correlationId={}", queueName, correlationId);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth())
                .post(RequestBody.create(requestBody, JSON_MEDIA_TYPE))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to publish message: HTTP " + response.code()
                        + " - " + (response.body() != null ? response.body().string() : ""));
            }
            log.info("Message published to queue '{}' successfully", queueName);
        }
    }

    /**
     * Gets (consumes) the next message from a queue via the RabbitMQ Management HTTP API.
     * Returns null if no message is available.
     *
     * @param queueName the source queue
     * @return the message payload as a string, or null if the queue is empty
     */
    public String getMessage(String queueName) throws IOException {
        String vhost = encodeVhost(appConfig.getRabbitmqVhost());
        String url = String.format("%s/api/queues/%s/%s/get",
                appConfig.getRabbitmqManagementHost(), vhost, encodeQueueName(queueName));

        Map<String, Object> body = new HashMap<>();
        body.put("count", 1);
        body.put("ackmode", "ack_requeue_false");
        body.put("encoding", "auto");
        body.put("truncate", 50000);

        String requestBody = objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", buildBasicAuth())
                .post(RequestBody.create(requestBody, JSON_MEDIA_TYPE))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                log.warn("Queue '{}' not found", queueName);
                return null;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get message: HTTP " + response.code()
                        + " - " + (response.body() != null ? response.body().string() : ""));
            }
            String responseJson = response.body() != null ? response.body().string() : "[]";
            JsonNode messages = objectMapper.readTree(responseJson);
            if (messages.isArray() && !messages.isEmpty()) {
                String payload = messages.get(0).path("payload").asText();
                log.info("Message retrieved from queue '{}': {}", queueName, payload);
                return payload;
            }
            log.debug("No messages available in queue '{}'", queueName);
            return null;
        }
    }

    /**
     * Polls the response queue for a message matching the given correlationId.
     * Uses Awaitility-compatible approach: returns null if not found, caller handles retries.
     */
    public OrderResponse getOrderResponse(String queueName, String correlationId) throws IOException {
        String payload = getMessage(queueName);
        if (payload == null) {
            return null;
        }
        OrderResponse orderResponse = objectMapper.readValue(payload, OrderResponse.class);
        if (correlationId.equals(orderResponse.getCorrelationId())) {
            log.info("Final response received: correlationId={}, status={}, orderId={}",
                    orderResponse.getCorrelationId(), orderResponse.getStatus(), orderResponse.getOrderId());
            return orderResponse;
        }
        log.debug("Message correlationId '{}' does not match expected '{}'",
                orderResponse.getCorrelationId(), correlationId);
        return null;
    }

    private String buildBasicAuth() {
        return Credentials.basic(appConfig.getRabbitmqUsername(), appConfig.getRabbitmqPassword());
    }

    private String encodeVhost(String vhost) {
        if ("/".equals(vhost)) {
            return "%2F";
        }
        return URLEncoder.encode(vhost, StandardCharsets.UTF_8);
    }

    private String encodeQueueName(String queueName) {
        return URLEncoder.encode(queueName, StandardCharsets.UTF_8);
    }
}
