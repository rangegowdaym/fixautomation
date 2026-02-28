package com.fixautomation.hooks;

import com.fixautomation.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CucumberHooks {

    private static final Logger log = LoggerFactory.getLogger(CucumberHooks.class);

    @Autowired
    private TestContext testContext;

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        log.info("=== Starting scenario: {} [{}] ===", scenario.getName(), scenario.getId());
        Allure.step("Starting scenario: " + scenario.getName());
        testContext.reset();
    }

    @After(order = 0)
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("=== Scenario FAILED: {} ===", scenario.getName());
            attachContextToAllure(scenario);
        } else {
            log.info("=== Scenario PASSED: {} ===", scenario.getName());
        }
        testContext.reset();
    }

    private void attachContextToAllure(Scenario scenario) {
        try {
            if (testContext.getOrderRequest() != null) {
                String reqInfo = String.format("correlationId=%s, symbol=%s",
                        testContext.getOrderRequest().getCorrelationId(),
                        testContext.getOrderRequest().getSymbol());
                Allure.addAttachment("OrderRequest", reqInfo);
            }
            if (testContext.getAckResponse() != null) {
                String ackInfo = String.format("status=%s, correlationId=%s",
                        testContext.getAckResponse().getStatus(),
                        testContext.getAckResponse().getCorrelationId());
                Allure.addAttachment("AckResponse", ackInfo);
            }
            if (testContext.getOrderResponse() != null) {
                String respInfo = String.format("orderId=%s, status=%s, executionType=%s",
                        testContext.getOrderResponse().getOrderId(),
                        testContext.getOrderResponse().getStatus(),
                        testContext.getOrderResponse().getExecutionType());
                Allure.addAttachment("OrderResponse", respInfo);
            }
        } catch (Exception e) {
            log.warn("Failed to attach context to Allure report: {}", e.getMessage());
        }
    }
}
