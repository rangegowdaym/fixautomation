package com.fixautomation.context;

import com.fixautomation.model.AckResponse;
import com.fixautomation.model.OrderRequest;
import com.fixautomation.model.OrderResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Thread-safe scenario-scoped context for Cucumber parallel execution.
 * Each scenario thread gets its own instance via Spring's prototype scope.
 */
@Component
@Scope("cucumber-glue")
public class TestContext {

    private OrderRequest orderRequest;
    private AckResponse ackResponse;
    private OrderResponse orderResponse;
    private String rawAckMessage;
    private String rawOrderResponse;

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

    public void reset() {
        this.orderRequest = null;
        this.ackResponse = null;
        this.orderResponse = null;
        this.rawAckMessage = null;
        this.rawOrderResponse = null;
    }
}
