package com.fixautomation.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixautomation.config.AppConfig;
import com.fixautomation.model.OrderResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Management API client using REST Assured.
 * Provides publish and getMessage operations with Basic Auth.
 */
@Component
public class RabbitMQManagementService {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQManagementService.class);

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitMQManagementService(AppConfig appConfig, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes a message to a queue via the RabbitMQ Management HTTP API.
     *
     * @param queueName     the target queue
     * @param payload       the JSON payload as a string
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

        Response response = RestAssured
                .given()
                    .auth().preemptive().basic(appConfig.getRabbitmqUsername(), appConfig.getRabbitmqPassword())
                    .contentType("application/json")
                    .body(requestBody)
                    .log().method().log().uri().log().body()
                .when()
                    .post(url)
                .then()
                    .log().status().log().body()
                    .extract().response();

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to publish message: HTTP " + response.statusCode()
                    + " - " + response.body().asString());
        }
        log.info("Message published to queue '{}' successfully", queueName);
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

        Response response = RestAssured
                .given()
                    .auth().preemptive().basic(appConfig.getRabbitmqUsername(), appConfig.getRabbitmqPassword())
                    .contentType("application/json")
                    .body(requestBody)
                    .log().method().log().uri().log().body()
                .when()
                    .post(url)
                .then()
                    .log().status().log().body()
                    .extract().response();

        if (response.statusCode() == 404) {
            log.warn("Queue '{}' not found", queueName);
            return null;
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to get message: HTTP " + response.statusCode()
                    + " - " + response.body().asString());
        }

        String responseJson = response.body().asString();
        JsonNode messages = objectMapper.readTree(responseJson);
        if (messages.isArray() && !messages.isEmpty()) {
            String msgPayload = messages.get(0).path("payload").asText();
            log.info("Message retrieved from queue '{}': {}", queueName, msgPayload);
            return msgPayload;
        }
        log.debug("No messages available in queue '{}'", queueName);
        return null;
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
