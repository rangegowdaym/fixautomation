package com.fixautomation.models;

/**
 * Domain model representing a FIX ExecutionReport for use in test assertions and reports.
 */
public class ExecutionReportModel {

    private String execId;
    private String clOrdId;
    private String orderId;
    private String execType;
    private String ordStatus;
    private String symbol;
    private String side;
    private double orderQty;
    private double cumQty;
    private double leavesQty;
    private double avgPx;
    private double lastPx;
    private double lastQty;
    private String text;
    private String rawMessage;

    public ExecutionReportModel() {
    }

    public String getExecId() {
        return execId;
    }

    public ExecutionReportModel setExecId(String execId) {
        this.execId = execId;
        return this;
    }

    public String getClOrdId() {
        return clOrdId;
    }

    public ExecutionReportModel setClOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public ExecutionReportModel setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getExecType() {
        return execType;
    }

    public ExecutionReportModel setExecType(String execType) {
        this.execType = execType;
        return this;
    }

    public String getOrdStatus() {
        return ordStatus;
    }

    public ExecutionReportModel setOrdStatus(String ordStatus) {
        this.ordStatus = ordStatus;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public ExecutionReportModel setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getSide() {
        return side;
    }

    public ExecutionReportModel setSide(String side) {
        this.side = side;
        return this;
    }

    public double getOrderQty() {
        return orderQty;
    }

    public ExecutionReportModel setOrderQty(double orderQty) {
        this.orderQty = orderQty;
        return this;
    }

    public double getCumQty() {
        return cumQty;
    }

    public ExecutionReportModel setCumQty(double cumQty) {
        this.cumQty = cumQty;
        return this;
    }

    public double getLeavesQty() {
        return leavesQty;
    }

    public ExecutionReportModel setLeavesQty(double leavesQty) {
        this.leavesQty = leavesQty;
        return this;
    }

    public double getAvgPx() {
        return avgPx;
    }

    public ExecutionReportModel setAvgPx(double avgPx) {
        this.avgPx = avgPx;
        return this;
    }

    public double getLastPx() {
        return lastPx;
    }

    public ExecutionReportModel setLastPx(double lastPx) {
        this.lastPx = lastPx;
        return this;
    }

    public double getLastQty() {
        return lastQty;
    }

    public ExecutionReportModel setLastQty(double lastQty) {
        this.lastQty = lastQty;
        return this;
    }

    public String getText() {
        return text;
    }

    public ExecutionReportModel setText(String text) {
        this.text = text;
        return this;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public ExecutionReportModel setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
        return this;
    }

    @Override
    public String toString() {
        return "ExecutionReportModel{execId='" + execId + "', clOrdId='" + clOrdId +
               "', orderId='" + orderId + "', execType='" + execType +
               "', ordStatus='" + ordStatus + "', symbol='" + symbol +
               "', side='" + side + "', orderQty=" + orderQty +
               ", cumQty=" + cumQty + ", leavesQty=" + leavesQty +
               ", avgPx=" + avgPx + '}';
    }
}
