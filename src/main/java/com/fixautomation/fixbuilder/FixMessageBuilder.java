package com.fixautomation.fixbuilder;

import com.fixautomation.fixclient.FixResponseStore;
import com.fixautomation.fixclient.FixSessionManager;
import com.fixautomation.utils.FixMessageLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.Message;
import quickfix.SessionNotFound;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderStatusRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fluent FIX message builder for constructing and sending FIX 4.4 messages.
 *
 * <p>Example usage:
 * <pre>
 *   fixMessageBuilder.newOrderSingle()
 *       .symbol("AAPL")
 *       .side(Side.BUY)
 *       .quantity(100)
 *       .price(150.00)
 *       .send();
 * </pre>
 */
@Component
public class FixMessageBuilder {

    private static final Logger log = LogManager.getLogger(FixMessageBuilder.class);

    private final FixSessionManager fixSessionManager;
    private final FixResponseStore fixResponseStore;

    @Autowired
    public FixMessageBuilder(FixSessionManager fixSessionManager, FixResponseStore fixResponseStore) {
        this.fixSessionManager = fixSessionManager;
        this.fixResponseStore = fixResponseStore;
    }

    /**
     * Creates a new NewOrderSingle builder.
     *
     * @return a builder for NewOrderSingle
     */
    public NewOrderSingleBuilder newOrderSingle() {
        return new NewOrderSingleBuilder();
    }

    /**
     * Creates a new OrderCancelRequest builder.
     *
     * @return a builder for OrderCancelRequest
     */
    public OrderCancelRequestBuilder orderCancelRequest() {
        return new OrderCancelRequestBuilder();
    }

    /**
     * Creates a new OrderCancelReplaceRequest builder.
     *
     * @return a builder for OrderCancelReplaceRequest
     */
    public OrderCancelReplaceBuilder orderCancelReplace() {
        return new OrderCancelReplaceBuilder();
    }

    /**
     * Creates a new OrderStatusRequest builder.
     *
     * @return a builder for OrderStatusRequest
     */
    public OrderStatusRequestBuilder orderStatusRequest() {
        return new OrderStatusRequestBuilder();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NewOrderSingle builder
    // ─────────────────────────────────────────────────────────────────────────

    public class NewOrderSingleBuilder {
        private String clOrdId = UUID.randomUUID().toString().replace("-", "");
        private String symbolValue;
        private char sideValue = Side.BUY;
        private double quantityValue;
        private double priceValue;
        private char ordTypeValue = OrdType.LIMIT;
        private String accountValue;

        public NewOrderSingleBuilder clOrdId(String clOrdId) {
            this.clOrdId = clOrdId;
            return this;
        }

        public NewOrderSingleBuilder symbol(String symbol) {
            this.symbolValue = symbol;
            return this;
        }

        public NewOrderSingleBuilder side(String side) {
            this.sideValue = "SELL".equalsIgnoreCase(side) ? Side.SELL : Side.BUY;
            return this;
        }

        public NewOrderSingleBuilder side(char side) {
            this.sideValue = side;
            return this;
        }

        public NewOrderSingleBuilder quantity(double qty) {
            this.quantityValue = qty;
            return this;
        }

        public NewOrderSingleBuilder price(double price) {
            this.priceValue = price;
            return this;
        }

        public NewOrderSingleBuilder orderType(char ordType) {
            this.ordTypeValue = ordType;
            return this;
        }

        public NewOrderSingleBuilder account(String account) {
            this.accountValue = account;
            return this;
        }

        /**
         * Builds the NewOrderSingle message without sending.
         *
         * @return the constructed FIX message
         */
        public NewOrderSingle build() {
            NewOrderSingle order = new NewOrderSingle(
                    new ClOrdID(clOrdId),
                    new Side(sideValue),
                    new TransactTime(LocalDateTime.now()),
                    new OrdType(ordTypeValue)
            );
            order.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION));
            order.set(new Symbol(symbolValue));
            order.set(new OrderQty(quantityValue));
            if (ordTypeValue == OrdType.LIMIT || ordTypeValue == OrdType.STOP_LIMIT) {
                order.set(new Price(priceValue));
            }
            if (accountValue != null) {
                order.set(new Account(accountValue));
            }
            FixMessageLogger.log(order, "NewOrderSingle");
            return order;
        }

        /**
         * Builds and sends the NewOrderSingle message.
         *
         * @return the ClOrdID used for this order
         */
        public String send() {
            Message msg = build();
            try {
                fixSessionManager.send(msg);
                log.info("NewOrderSingle sent: ClOrdID={}", clOrdId);
            } catch (SessionNotFound | IllegalStateException e) {
                log.error("Failed to send NewOrderSingle: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send NewOrderSingle: " + e.getMessage(), e);
            }
            return clOrdId;
        }

        public String getClOrdId() {
            return clOrdId;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrderCancelRequest builder
    // ─────────────────────────────────────────────────────────────────────────

    public class OrderCancelRequestBuilder {
        private String clOrdId = UUID.randomUUID().toString().replace("-", "");
        private String origClOrdIdValue;
        private String orderIdValue;
        private String symbolValue;
        private char sideValue = Side.BUY;

        public OrderCancelRequestBuilder clOrdId(String clOrdId) {
            this.clOrdId = clOrdId;
            return this;
        }

        public OrderCancelRequestBuilder origClOrdId(String origClOrdId) {
            this.origClOrdIdValue = origClOrdId;
            return this;
        }

        public OrderCancelRequestBuilder orderId(String orderId) {
            this.orderIdValue = orderId;
            return this;
        }

        public OrderCancelRequestBuilder symbol(String symbol) {
            this.symbolValue = symbol;
            return this;
        }

        public OrderCancelRequestBuilder side(String side) {
            this.sideValue = "SELL".equalsIgnoreCase(side) ? Side.SELL : Side.BUY;
            return this;
        }

        public OrderCancelRequest build() {
            OrderCancelRequest cancel = new OrderCancelRequest(
                    new OrigClOrdID(origClOrdIdValue),
                    new ClOrdID(clOrdId),
                    new Side(sideValue),
                    new TransactTime(LocalDateTime.now())
            );
            cancel.set(new Symbol(symbolValue));
            if (orderIdValue != null) {
                cancel.set(new OrderID(orderIdValue));
            }
            FixMessageLogger.log(cancel, "OrderCancelRequest");
            return cancel;
        }

        public String send() {
            Message msg = build();
            try {
                fixSessionManager.send(msg);
                log.info("OrderCancelRequest sent: ClOrdID={}, OrigClOrdID={}", clOrdId, origClOrdIdValue);
            } catch (SessionNotFound | IllegalStateException e) {
                log.error("Failed to send OrderCancelRequest: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send OrderCancelRequest: " + e.getMessage(), e);
            }
            return clOrdId;
        }

        public String getClOrdId() {
            return clOrdId;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrderCancelReplaceRequest builder
    // ─────────────────────────────────────────────────────────────────────────

    public class OrderCancelReplaceBuilder {
        private String clOrdId = UUID.randomUUID().toString().replace("-", "");
        private String origClOrdIdValue;
        private String symbolValue;
        private char sideValue = Side.BUY;
        private double quantityValue;
        private double priceValue;
        private char ordTypeValue = OrdType.LIMIT;

        public OrderCancelReplaceBuilder clOrdId(String clOrdId) {
            this.clOrdId = clOrdId;
            return this;
        }

        public OrderCancelReplaceBuilder origClOrdId(String origClOrdId) {
            this.origClOrdIdValue = origClOrdId;
            return this;
        }

        public OrderCancelReplaceBuilder symbol(String symbol) {
            this.symbolValue = symbol;
            return this;
        }

        public OrderCancelReplaceBuilder side(String side) {
            this.sideValue = "SELL".equalsIgnoreCase(side) ? Side.SELL : Side.BUY;
            return this;
        }

        public OrderCancelReplaceBuilder quantity(double qty) {
            this.quantityValue = qty;
            return this;
        }

        public OrderCancelReplaceBuilder price(double price) {
            this.priceValue = price;
            return this;
        }

        public OrderCancelReplaceRequest build() {
            OrderCancelReplaceRequest replace = new OrderCancelReplaceRequest(
                    new OrigClOrdID(origClOrdIdValue),
                    new ClOrdID(clOrdId),
                    new Side(sideValue),
                    new TransactTime(LocalDateTime.now()),
                    new OrdType(ordTypeValue)
            );
            replace.set(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE_NO_BROKER_INTERVENTION));
            replace.set(new Symbol(symbolValue));
            replace.set(new OrderQty(quantityValue));
            if (ordTypeValue == OrdType.LIMIT) {
                replace.set(new Price(priceValue));
            }
            FixMessageLogger.log(replace, "OrderCancelReplaceRequest");
            return replace;
        }

        public String send() {
            Message msg = build();
            try {
                fixSessionManager.send(msg);
                log.info("OrderCancelReplaceRequest sent: ClOrdID={}, OrigClOrdID={}", clOrdId, origClOrdIdValue);
            } catch (SessionNotFound | IllegalStateException e) {
                log.error("Failed to send OrderCancelReplaceRequest: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send OrderCancelReplaceRequest: " + e.getMessage(), e);
            }
            return clOrdId;
        }

        public String getClOrdId() {
            return clOrdId;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OrderStatusRequest builder
    // ─────────────────────────────────────────────────────────────────────────

    public class OrderStatusRequestBuilder {
        private String clOrdId = UUID.randomUUID().toString().replace("-", "");
        private String symbolValue;
        private char sideValue = Side.BUY;

        public OrderStatusRequestBuilder clOrdId(String clOrdId) {
            this.clOrdId = clOrdId;
            return this;
        }

        public OrderStatusRequestBuilder symbol(String symbol) {
            this.symbolValue = symbol;
            return this;
        }

        public OrderStatusRequestBuilder side(String side) {
            this.sideValue = "SELL".equalsIgnoreCase(side) ? Side.SELL : Side.BUY;
            return this;
        }

        public OrderStatusRequest build() {
            OrderStatusRequest statusReq = new OrderStatusRequest(
                    new ClOrdID(clOrdId),
                    new Side(sideValue)
            );
            statusReq.set(new Symbol(symbolValue));
            FixMessageLogger.log(statusReq, "OrderStatusRequest");
            return statusReq;
        }

        public String send() {
            Message msg = build();
            try {
                fixSessionManager.send(msg);
                log.info("OrderStatusRequest sent: ClOrdID={}", clOrdId);
            } catch (SessionNotFound | IllegalStateException e) {
                log.error("Failed to send OrderStatusRequest: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to send OrderStatusRequest: " + e.getMessage(), e);
            }
            return clOrdId;
        }

        public String getClOrdId() {
            return clOrdId;
        }
    }
}
