package com.fixautomation.models;

/**
 * Domain model representing a FIX order for use in test assertions and reports.
 */
public class FixOrderModel {

    private String clOrdId;
    private String orderId;
    private String symbol;
    private String side;
    private double quantity;
    private double price;
    private String ordType;
    private String ordStatus;
    private String account;

    public FixOrderModel() {
    }

    public String getClOrdId() {
        return clOrdId;
    }

    public FixOrderModel setClOrdId(String clOrdId) {
        this.clOrdId = clOrdId;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public FixOrderModel setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public FixOrderModel setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getSide() {
        return side;
    }

    public FixOrderModel setSide(String side) {
        this.side = side;
        return this;
    }

    public double getQuantity() {
        return quantity;
    }

    public FixOrderModel setQuantity(double quantity) {
        this.quantity = quantity;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public FixOrderModel setPrice(double price) {
        this.price = price;
        return this;
    }

    public String getOrdType() {
        return ordType;
    }

    public FixOrderModel setOrdType(String ordType) {
        this.ordType = ordType;
        return this;
    }

    public String getOrdStatus() {
        return ordStatus;
    }

    public FixOrderModel setOrdStatus(String ordStatus) {
        this.ordStatus = ordStatus;
        return this;
    }

    public String getAccount() {
        return account;
    }

    public FixOrderModel setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String toString() {
        return "FixOrderModel{clOrdId='" + clOrdId + "', orderId='" + orderId +
               "', symbol='" + symbol + "', side='" + side +
               "', quantity=" + quantity + ", price=" + price +
               ", ordType='" + ordType + "', ordStatus='" + ordStatus + "'}";
    }
}
