package com.fixautomation.steps;

import com.fixautomation.context.TestContext;
import com.fixautomation.model.AckResponse;
import com.fixautomation.model.OrderRequest;
import com.fixautomation.model.OrderResponse;
import com.fixautomation.rabbitmq.RabbitMQManagementService;
import com.fixautomation.config.AppConfig;
import com.fixautomation.utils.JsonValidator;
import com.fixautomation.utils.RetryUtils;
import com.fixautomation.websocket.WebSocketService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class OrderStepDefinitions {

    private static final Logger log = LogManager.getLogger(OrderStepDefinitions.class);

    private static final String DEFAULT_SYMBOL = "AAPL";
    private static final int DEFAULT_QUANTITY = 100;
    private static final double DEFAULT_PRICE = 150.00;
    private static final String SIDE_BUY = "BUY";
    private static final String ORDER_TYPE_LIMIT = "LIMIT";
    private static final String EXECUTION_TYPE_NEW = "NEW";
    private static final String EXECUTION_TYPE_MARKET = "MARKET";

    @Autowired
    private TestContext testContext;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RabbitMQManagementService rabbitMQManagementService;

    @Autowired
    private AppConfig appConfig;

    @Given("the test environment is configured")
    @Step("Verify test environment is configured")
    public void theTestEnvironmentIsConfigured() {
        assertNotNull(appConfig.getWebsocketUrl(), "WebSocket URL must be configured");
        assertNotNull(appConfig.getRabbitmqManagementHost(), "RabbitMQ Management host must be configured");
        log.info("Test environment configured: ws={}, rmq={}", appConfig.getWebsocketUrl(),
                appConfig.getRabbitmqManagementHost());
    }

    @Given("I send a new order via WebSocket")
    @Step("Send new order via WebSocket")
    public void iSendANewOrderViaWebSocket() throws Exception {
        OrderRequest orderRequest = buildDefaultOrderRequest();
        testContext.setOrderRequest(orderRequest);
        AckResponse ack = webSocketService.sendAndWaitForAck(orderRequest);
        testContext.setAckResponse(ack);
        log.info("Order sent via WebSocket: correlationId={}", orderRequest.getCorrelationId());
    }

    @Given("I send a new BUY order via WebSocket")
    @Step("Send new BUY order via WebSocket")
    public void iSendANewBuyOrderViaWebSocket() throws Exception {
        OrderRequest orderRequest = buildDefaultOrderRequest();
        orderRequest.setSide(SIDE_BUY);
        orderRequest.setExecutionType(EXECUTION_TYPE_MARKET);
        testContext.setOrderRequest(orderRequest);
        AckResponse ack = webSocketService.sendAndWaitForAck(orderRequest);
        testContext.setAckResponse(ack);
        log.info("BUY order sent via WebSocket: correlationId={}", orderRequest.getCorrelationId());
    }

    @Then("I should receive ACK")
    @Step("Verify ACK received")
    public void iShouldReceiveAck() {
        AckResponse ack = testContext.getAckResponse();
        assertNotNull(ack, "ACK response must not be null");
        assertTrue(ack.isAcknowledged(),
                "Expected ACK status but got: " + ack.getStatus());
        assertEquals(ack.getCorrelationId(), testContext.getOrderRequest().getCorrelationId(),
                "ACK correlationId must match request correlationId");
        log.info("ACK validated: correlationId={}, status={}", ack.getCorrelationId(), ack.getStatus());
    }

    @And("I should verify final response in RabbitMQ")
    @Step("Verify final response in RabbitMQ")
    public void iShouldVerifyFinalResponseInRabbitMQ() {
        String correlationId = testContext.getOrderRequest().getCorrelationId();
        String responseQueue = appConfig.getResponseQueue();

        OrderResponse orderResponse = RetryUtils.pollUntilPresent(
                () -> {
                    try {
                        return rabbitMQManagementService.getOrderResponse(responseQueue, correlationId);
                    } catch (Exception e) {
                        log.warn("Error polling RabbitMQ response queue: {}", e.getMessage());
                        return null;
                    }
                },
                appConfig.getPollingTimeoutSeconds(),
                appConfig.getPollingIntervalMillis(),
                "RabbitMQ final response for correlationId=" + correlationId
        );

        testContext.setOrderResponse(orderResponse);
        assertNotNull(orderResponse, "Final order response must not be null");
        assertEquals(orderResponse.getCorrelationId(), correlationId,
                "Response correlationId must match request correlationId");
        assertNotNull(orderResponse.getOrderId(), "orderId must be present in response");
        assertNotNull(orderResponse.getStatus(), "status must be present in response");
        log.info("Final response validated: correlationId={}, orderId={}, status={}",
                orderResponse.getCorrelationId(), orderResponse.getOrderId(), orderResponse.getStatus());
    }

    @And("the order response should have status {string}")
    @Step("Verify order response status is {0}")
    public void theOrderResponseShouldHaveStatus(String expectedStatus) {
        OrderResponse orderResponse = testContext.getOrderResponse();
        assertNotNull(orderResponse, "Order response must not be null");
        assertEquals(orderResponse.getStatus(), expectedStatus,
                "Order status mismatch");
        log.info("Order status verified: {}", expectedStatus);
    }

    @And("the order response should have executionType {string}")
    @Step("Verify order response executionType is {0}")
    public void theOrderResponseShouldHaveExecutionType(String expectedExecutionType) {
        OrderResponse orderResponse = testContext.getOrderResponse();
        assertNotNull(orderResponse, "Order response must not be null");
        assertEquals(orderResponse.getExecutionType(), expectedExecutionType,
                "Execution type mismatch");
        log.info("Execution type verified: {}", expectedExecutionType);
    }

    private OrderRequest buildDefaultOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setSymbol(DEFAULT_SYMBOL);
        request.setQuantity(DEFAULT_QUANTITY);
        request.setPrice(DEFAULT_PRICE);
        request.setSide(SIDE_BUY);
        request.setOrderType(ORDER_TYPE_LIMIT);
        request.setExecutionType(EXECUTION_TYPE_NEW);
        return request;
    }
}
