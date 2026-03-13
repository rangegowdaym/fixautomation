package com.fixautomation.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.Symbol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Validates order status-related FIX tags on a received FIX message.
 */
@Component
public class OrderStatusValidator {

    private static final Logger log = LogManager.getLogger(OrderStatusValidator.class);

    private final FixTagValidator fixTagValidator;

    @Autowired
    public OrderStatusValidator(FixTagValidator fixTagValidator) {
        this.fixTagValidator = fixTagValidator;
    }

    /**
     * Validates that the message has the expected OrdStatus (tag 39).
     *
     * @param message        the FIX message
     * @param expectedStatus the expected OrdStatus char (e.g. OrdStatus.NEW, OrdStatus.FILLED)
     */
    public void assertOrdStatus(Message message, char expectedStatus) {
        log.info("Validating OrdStatus(39): expected='{}'", expectedStatus);
        fixTagValidator.assertTagEquals(message, OrdStatus.FIELD, expectedStatus, "OrdStatus");
    }

    /**
     * Validates that the message has the expected OrdStatus by its string label.
     *
     * @param message        the FIX message
     * @param expectedStatus human-readable status (e.g. "NEW", "FILLED", "CANCELED")
     */
    public void assertOrdStatus(Message message, String expectedStatus) {
        char statusChar = resolveOrdStatus(expectedStatus);
        assertOrdStatus(message, statusChar);
    }

    /**
     * Validates that the ClOrdID (tag 11) matches the expected value.
     *
     * @param message         the FIX message
     * @param expectedClOrdId the expected ClOrdID
     */
    public void assertClOrdId(Message message, String expectedClOrdId) {
        log.info("Validating ClOrdID(11): expected='{}'", expectedClOrdId);
        fixTagValidator.assertTagEquals(message, ClOrdID.FIELD, expectedClOrdId, "ClOrdID");
    }

    /**
     * Validates that the Symbol (tag 55) matches the expected value.
     *
     * @param message        the FIX message
     * @param expectedSymbol the expected symbol
     */
    public void assertSymbol(Message message, String expectedSymbol) {
        log.info("Validating Symbol(55): expected='{}'", expectedSymbol);
        fixTagValidator.assertTagEquals(message, Symbol.FIELD, expectedSymbol, "Symbol");
    }

    /**
     * Validates that the OrderID (tag 37) is present and non-empty.
     *
     * @param message the FIX message
     */
    public void assertOrderIdPresent(Message message) {
        log.info("Validating OrderID(37) is present");
        fixTagValidator.assertTagPresent(message, OrderID.FIELD, "OrderID");
    }

    /**
     * Validates that the message contains an OrderID equal to the expected value.
     *
     * @param message         the FIX message
     * @param expectedOrderId the expected order ID
     */
    public void assertOrderId(Message message, String expectedOrderId) {
        log.info("Validating OrderID(37): expected='{}'", expectedOrderId);
        fixTagValidator.assertTagEquals(message, OrderID.FIELD, expectedOrderId, "OrderID");
    }

    /**
     * Resolves a human-readable OrdStatus string to its FIX char value.
     *
     * @param status the human-readable status string
     * @return the FIX OrdStatus char
     */
    private char resolveOrdStatus(String status) {
        return switch (status.toUpperCase()) {
            case "NEW"                  -> OrdStatus.NEW;
            case "PARTIALLY_FILLED",
                 "PARTIAL_FILL"        -> OrdStatus.PARTIALLY_FILLED;
            case "FILLED", "FILL"      -> OrdStatus.FILLED;
            case "DONE_FOR_DAY"        -> OrdStatus.DONE_FOR_DAY;
            case "CANCELED", "CANCEL"  -> OrdStatus.CANCELED;
            case "REPLACED", "REPLACE" -> OrdStatus.REPLACED;
            case "PENDING_CANCEL"      -> OrdStatus.PENDING_CANCEL;
            case "STOPPED"             -> OrdStatus.STOPPED;
            case "REJECTED", "REJECT"  -> OrdStatus.REJECTED;
            case "SUSPENDED"           -> OrdStatus.SUSPENDED;
            case "PENDING_NEW"         -> OrdStatus.PENDING_NEW;
            case "CALCULATED"          -> OrdStatus.CALCULATED;
            case "EXPIRED"             -> OrdStatus.EXPIRED;
            case "ACCEPTED_FOR_BIDDING"-> OrdStatus.ACCEPTED_FOR_BIDDING;
            case "PENDING_REPLACE"     -> OrdStatus.PENDING_REPLACE;
            default -> throw new IllegalArgumentException("Unknown OrdStatus: " + status);
        };
    }
}
