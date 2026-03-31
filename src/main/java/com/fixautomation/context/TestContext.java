package com.fixautomation.context;

import com.fixautomation.model.AckResponse;
import com.fixautomation.model.OrderRequest;
import com.fixautomation.model.OrderResponse;
import com.fixautomation.models.ExecutionReportModel;
import com.fixautomation.models.FixOrderModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import quickfix.Message;

/**
 * Thread-safe scenario-scoped context for Cucumber parallel execution.
 * Each scenario thread gets its own instance via Spring's prototype scope.
 * Holds both WebSocket/RabbitMQ state and FIX engine state.
 */
@Component
@Scope("cucumber-glue")
public class TestContext {

    // ── WebSocket / RabbitMQ state ────────────────────────────────────────────
    private OrderRequest orderRequest;
    private AckResponse ackResponse;
    private OrderResponse orderResponse;
    private String rawAckMessage;
    private String rawOrderResponse;

    // ── FIX engine state ──────────────────────────────────────────────────────
    private String fixClOrdId;
    private FixOrderModel fixOrderModel;
    private Message fixResponseMessage;
    private ExecutionReportModel executionReportModel;

    // ── WebSocket / RabbitMQ accessors ────────────────────────────────────────

    public OrderRequest getOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(OrderRequest orderRequest) {
        this.orderRequest = orderRequest;
    }

    public AckResponse getAckResponse() {
        return ackResponse;
    }

    public void setAckResponse(AckResponse ackResponse) {
        this.ackResponse = ackResponse;
    }

    public OrderResponse getOrderResponse() {
        return orderResponse;
    }

    public void setOrderResponse(OrderResponse orderResponse) {
        this.orderResponse = orderResponse;
    }

    public String getRawAckMessage() {
        return rawAckMessage;
    }

    public void setRawAckMessage(String rawAckMessage) {
        this.rawAckMessage = rawAckMessage;
    }

    public String getRawOrderResponse() {
        return rawOrderResponse;
    }

    public void setRawOrderResponse(String rawOrderResponse) {
        this.rawOrderResponse = rawOrderResponse;
    }

    // ── FIX engine accessors ──────────────────────────────────────────────────

    public String getFixClOrdId() {
        return fixClOrdId;
    }

    public void setFixClOrdId(String fixClOrdId) {
        this.fixClOrdId = fixClOrdId;
    }

    public FixOrderModel getFixOrderModel() {
        return fixOrderModel;
    }

    public void setFixOrderModel(FixOrderModel fixOrderModel) {
        this.fixOrderModel = fixOrderModel;
    }

    public Message getFixResponseMessage() {
        return fixResponseMessage;
    }

    public void setFixResponseMessage(Message fixResponseMessage) {
        this.fixResponseMessage = fixResponseMessage;
    }

    public ExecutionReportModel getExecutionReportModel() {
        return executionReportModel;
    }

    public void setExecutionReportModel(ExecutionReportModel executionReportModel) {
        this.executionReportModel = executionReportModel;
    }

    /**
     * Resets all context state between scenarios.
     */
    public void reset() {
        this.orderRequest = null;
        this.ackResponse = null;
        this.orderResponse = null;
        this.rawAckMessage = null;
        this.rawOrderResponse = null;
        this.fixClOrdId = null;
        this.fixOrderModel = null;
        this.fixResponseMessage = null;
        this.executionReportModel = null;
    }
}
