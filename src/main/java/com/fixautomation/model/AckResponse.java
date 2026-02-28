package com.fixautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AckResponse {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAcknowledged() {
        return "ACK".equalsIgnoreCase(status) || "ACKNOWLEDGED".equalsIgnoreCase(status);
    }
}
