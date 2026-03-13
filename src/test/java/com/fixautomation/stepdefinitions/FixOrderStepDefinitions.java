package com.fixautomation.stepdefinitions;

import com.fixautomation.context.TestContext;
import com.fixautomation.fixbuilder.FixMessageBuilder;
import com.fixautomation.fixclient.FixResponseStore;
import com.fixautomation.fixclient.FixSessionManager;
import com.fixautomation.models.ExecutionReportModel;
import com.fixautomation.models.FixOrderModel;
import com.fixautomation.utils.FixMessageLogger;
import com.fixautomation.utils.RetryUtils;
import com.fixautomation.validators.ExecutionReportValidator;
import com.fixautomation.validators.OrderStatusValidator;
import com.fixautomation.config.FixConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Symbol;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Cucumber step definitions for FIX Engine inbound scenario testing.
 * Wires together FixMessageBuilder, FixSessionManager, FixResponseStore and validators.
 */
public class FixOrderStepDefinitions {

    private static final Logger log = LogManager.getLogger(FixOrderStepDefinitions.class);

    @Autowired
    private TestContext testContext;

    @Autowired
    private FixSessionManager fixSessionManager;

    @Autowired
    private FixMessageBuilder fixMessageBuilder;

    @Autowired
    private FixResponseStore fixResponseStore;

    @Autowired
    private ExecutionReportValidator executionReportValidator;

    @Autowired
    private OrderStatusValidator orderStatusValidator;

    @Autowired
    private FixConfig fixConfig;

    // ── Session setup ─────────────────────────────────────────────────────────

    @Given("FIX session is connected")
    @Step("Verify FIX session is connected")
    public void fixSessionIsConnected() {
        assertTrue(fixSessionManager.isConnected(),
                "FIX session must be connected before sending messages");
        log.info("FIX session is connected with {} active sessions",
                fixSessionManager.getSessions().size());
        Allure.step("FIX session verified: " + fixSessionManager.getSessions().size() + " active sessions");
    }

    // ── NewOrderSingle ────────────────────────────────────────────────────────

    @When("I send a NewOrderSingle with symbol {string} quantity {int}")
    @Step("Send NewOrderSingle: symbol={0}, quantity={1}")
    public void iSendNewOrderSingle(String symbol, int quantity) {
        log.info("Sending NewOrderSingle: symbol={}, quantity={}", symbol, quantity);
        FixOrderModel order = new FixOrderModel()
                .setSymbol(symbol)
                .setQuantity(quantity)
                .setPrice(150.00)
                .setSide("BUY")
                .setOrdType("LIMIT");
        testContext.setFixOrderModel(order);

        String clOrdId = fixMessageBuilder.newOrderSingle()
                .symbol(symbol)
                .side("BUY")
                .quantity(quantity)
                .price(150.00)
                .send();

        order.setClOrdId(clOrdId);
        testContext.setFixClOrdId(clOrdId);
        Allure.addAttachment("NewOrderSingle ClOrdID", clOrdId);
        log.info("NewOrderSingle sent: ClOrdID={}", clOrdId);
    }

    @When("I send a NewOrderSingle with symbol {string} side {string} quantity {int} price {double}")
    @Step("Send NewOrderSingle: symbol={0}, side={1}, quantity={2}, price={3}")
    public void iSendNewOrderSingleFull(String symbol, String side, int quantity, double price) {
        log.info("Sending NewOrderSingle: symbol={}, side={}, qty={}, price={}", symbol, side, quantity, price);
        FixOrderModel order = new FixOrderModel()
                .setSymbol(symbol)
                .setQuantity(quantity)
                .setPrice(price)
                .setSide(side)
                .setOrdType("LIMIT");
        testContext.setFixOrderModel(order);

        String clOrdId = fixMessageBuilder.newOrderSingle()
                .symbol(symbol)
                .side(side)
                .quantity(quantity)
                .price(price)
                .send();

        order.setClOrdId(clOrdId);
        testContext.setFixClOrdId(clOrdId);
        Allure.addAttachment("NewOrderSingle ClOrdID", clOrdId);
        log.info("NewOrderSingle sent: ClOrdID={}", clOrdId);
    }

    // ── OrderCancelRequest ────────────────────────────────────────────────────

    @When("I send an OrderCancelRequest for the last order")
    @Step("Send OrderCancelRequest for last order")
    public void iSendOrderCancelRequest() {
        String origClOrdId = testContext.getFixClOrdId();
        assertNotNull(origClOrdId, "OrigClOrdID must be set from a previous order step");

        String clOrdId = fixMessageBuilder.orderCancelRequest()
                .origClOrdId(origClOrdId)
                .symbol(testContext.getFixOrderModel().getSymbol())
                .side(testContext.getFixOrderModel().getSide())
                .send();

        testContext.setFixClOrdId(clOrdId);
        Allure.addAttachment("OrderCancelRequest ClOrdID", clOrdId);
        log.info("OrderCancelRequest sent: ClOrdID={}, OrigClOrdID={}", clOrdId, origClOrdId);
    }

    // ── OrderCancelReplaceRequest ─────────────────────────────────────────────

    @When("I send an OrderCancelReplaceRequest with new quantity {int} and price {double}")
    @Step("Send OrderCancelReplaceRequest: newQty={0}, newPrice={1}")
    public void iSendOrderCancelReplace(int newQty, double newPrice) {
        String origClOrdId = testContext.getFixClOrdId();
        assertNotNull(origClOrdId, "OrigClOrdID must be set from a previous order step");
        FixOrderModel order = testContext.getFixOrderModel();

        String clOrdId = fixMessageBuilder.orderCancelReplace()
                .origClOrdId(origClOrdId)
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(newQty)
                .price(newPrice)
                .send();

        order.setQuantity(newQty);
        order.setPrice(newPrice);
        testContext.setFixClOrdId(clOrdId);
        Allure.addAttachment("OrderCancelReplaceRequest ClOrdID", clOrdId);
        log.info("OrderCancelReplaceRequest sent: ClOrdID={}, OrigClOrdID={}", clOrdId, origClOrdId);
    }

    // ── OrderStatusRequest ────────────────────────────────────────────────────

    @When("I send an OrderStatusRequest for the last order")
    @Step("Send OrderStatusRequest for last order")
    public void iSendOrderStatusRequest() {
        String clOrdId = testContext.getFixClOrdId();
        assertNotNull(clOrdId, "ClOrdID must be set from a previous order step");
        FixOrderModel order = testContext.getFixOrderModel();

        fixMessageBuilder.orderStatusRequest()
                .clOrdId(clOrdId)
                .symbol(order.getSymbol())
                .side(order.getSide())
                .send();

        log.info("OrderStatusRequest sent: ClOrdID={}", clOrdId);
    }

    // ── ExecutionReport assertions ────────────────────────────────────────────

    @Then("ExecutionReport status should be {string}")
    @Step("Verify ExecutionReport OrdStatus={0}")
    public void executionReportStatusShouldBe(String expectedStatus) {
        String clOrdId = testContext.getFixClOrdId();
        assertNotNull(clOrdId, "ClOrdID not set — ensure a FIX order step was executed first");

        Message response = waitForFixResponse(clOrdId, "ExecutionReport[OrdStatus=" + expectedStatus + "]");
        testContext.setFixResponseMessage(response);
        attachExecutionReport(response);

        orderStatusValidator.assertOrdStatus(response, expectedStatus);
        log.info("ExecutionReport OrdStatus validated: expected={}", expectedStatus);
    }

    @Then("ExecutionReport ClOrdID should match the sent order")
    @Step("Verify ExecutionReport ClOrdID matches sent order")
    public void executionReportClOrdIdShouldMatch() {
        String expectedClOrdId = testContext.getFixClOrdId();
        assertNotNull(expectedClOrdId, "ClOrdID not set");
        Message response = testContext.getFixResponseMessage();
        assertNotNull(response, "FIX response message not yet received");
        orderStatusValidator.assertClOrdId(response, expectedClOrdId);
        log.info("ExecutionReport ClOrdID validated: {}", expectedClOrdId);
    }

    @Then("ExecutionReport should contain a valid OrderID")
    @Step("Verify ExecutionReport has a valid OrderID")
    public void executionReportShouldHaveOrderId() {
        Message response = getStoredOrWaitForResponse();
        orderStatusValidator.assertOrderIdPresent(response);
        try {
            String orderId = response.getString(OrderID.FIELD);
            testContext.getFixOrderModel().setOrderId(orderId);
            Allure.addAttachment("OrderID", orderId);
            log.info("OrderID validated: {}", orderId);
        } catch (FieldNotFound e) {
            throw new AssertionError("OrderID field not found", e);
        }
    }

    @Then("ExecutionReport Symbol should be {string}")
    @Step("Verify ExecutionReport Symbol={0}")
    public void executionReportSymbolShouldBe(String expectedSymbol) {
        Message response = getStoredOrWaitForResponse();
        orderStatusValidator.assertSymbol(response, expectedSymbol);
        log.info("ExecutionReport Symbol validated: {}", expectedSymbol);
    }

    @Then("ExecutionReport ExecType should be {string}")
    @Step("Verify ExecutionReport ExecType={0}")
    public void executionReportExecTypeShouldBe(String expectedExecType) {
        Message response = getStoredOrWaitForResponse();
        executionReportValidator.assertExecType(response, expectedExecType);
        log.info("ExecutionReport ExecType validated: {}", expectedExecType);
    }

    @Then("ExecutionReport OrderQty should be {int}")
    @Step("Verify ExecutionReport OrderQty={0}")
    public void executionReportOrderQtyShouldBe(int expectedQty) {
        Message response = getStoredOrWaitForResponse();
        executionReportValidator.assertOrderQty(response, expectedQty);
        log.info("ExecutionReport OrderQty validated: {}", expectedQty);
    }

    @Then("ExecutionReport mandatory fields should be present")
    @Step("Verify ExecutionReport mandatory fields are present")
    public void executionReportMandatoryFieldsShouldBePresent() {
        Message response = getStoredOrWaitForResponse();
        executionReportValidator.assertMandatoryFields(response);
        populateExecutionReportModel(response);
        log.info("ExecutionReport mandatory fields validated");
    }

    @Then("I should receive an ExecutionReport")
    @Step("Verify ExecutionReport received")
    public void iShouldReceiveExecutionReport() {
        String clOrdId = testContext.getFixClOrdId();
        assertNotNull(clOrdId, "ClOrdID not set");
        Message response = waitForFixResponse(clOrdId, "ExecutionReport");
        testContext.setFixResponseMessage(response);
        attachExecutionReport(response);
        assertNotNull(response, "ExecutionReport must be received");
        log.info("ExecutionReport received for ClOrdID={}", clOrdId);
    }

    @And("the ExecutionReport should indicate the order was rejected")
    @Step("Verify ExecutionReport indicates order rejection")
    public void executionReportShouldIndicateRejection() {
        Message response = getStoredOrWaitForResponse();
        orderStatusValidator.assertOrdStatus(response, "REJECTED");
        log.info("ExecutionReport rejection validated");
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    private Message waitForFixResponse(String clOrdId, String description) {
        return RetryUtils.pollUntilPresent(
                () -> fixResponseStore.getResponse(clOrdId),
                fixConfig.getResponseTimeoutSeconds(),
                fixConfig.getPollIntervalMillis(),
                "FIX response [" + description + "] for ClOrdID=" + clOrdId
        );
    }

    private Message getStoredOrWaitForResponse() {
        Message stored = testContext.getFixResponseMessage();
        if (stored != null) {
            return stored;
        }
        String clOrdId = testContext.getFixClOrdId();
        assertNotNull(clOrdId, "ClOrdID not set — ensure a FIX order step was executed first");
        Message response = waitForFixResponse(clOrdId, "ExecutionReport");
        testContext.setFixResponseMessage(response);
        attachExecutionReport(response);
        return response;
    }

    private void attachExecutionReport(Message message) {
        if (message != null) {
            String rawMsg = FixMessageLogger.toRawString(message);
            Allure.addAttachment("ExecutionReport (raw FIX)", rawMsg);
            log.debug("ExecutionReport attached to Allure: {}", rawMsg);
        }
    }

    private void populateExecutionReportModel(Message message) {
        try {
            ExecutionReportModel model = new ExecutionReportModel()
                    .setExecId(safeGetString(message, ExecID.FIELD))
                    .setClOrdId(safeGetString(message, ClOrdID.FIELD))
                    .setOrderId(safeGetString(message, OrderID.FIELD))
                    .setExecType(safeGetString(message, ExecType.FIELD))
                    .setOrdStatus(safeGetString(message, OrdStatus.FIELD))
                    .setSymbol(safeGetString(message, Symbol.FIELD))
                    .setOrderQty(safeGetDouble(message, OrderQty.FIELD))
                    .setCumQty(safeGetDouble(message, CumQty.FIELD))
                    .setLeavesQty(safeGetDouble(message, LeavesQty.FIELD))
                    .setAvgPx(safeGetDouble(message, AvgPx.FIELD))
                    .setRawMessage(FixMessageLogger.toRawString(message));
            testContext.setExecutionReportModel(model);
            log.debug("ExecutionReportModel populated: {}", model);
        } catch (Exception e) {
            log.warn("Failed to populate ExecutionReportModel: {}", e.getMessage());
        }
    }

    private String safeGetString(Message message, int tag) {
        try {
            return message.getString(tag);
        } catch (FieldNotFound e) {
            return null;
        }
    }

    private double safeGetDouble(Message message, int tag) {
        try {
            return message.getDouble(tag);
        } catch (FieldNotFound e) {
            return 0.0;
        }
    }
}
