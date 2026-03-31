package com.fixautomation.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.Message;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.OrderQty;
import quickfix.field.Text;

import static org.testng.Assert.assertNotNull;

/**
 * Validates ExecutionReport-specific FIX tags.
 */
@Component
public class ExecutionReportValidator {

    private static final Logger log = LogManager.getLogger(ExecutionReportValidator.class);

    private final FixTagValidator fixTagValidator;
    private final OrderStatusValidator orderStatusValidator;

    @Autowired
    public ExecutionReportValidator(FixTagValidator fixTagValidator,
                                    OrderStatusValidator orderStatusValidator) {
        this.fixTagValidator = fixTagValidator;
        this.orderStatusValidator = orderStatusValidator;
    }

    /**
     * Validates mandatory ExecutionReport fields are present.
     *
     * @param message the FIX ExecutionReport message
     */
    public void assertMandatoryFields(Message message) {
        assertNotNull(message, "ExecutionReport must not be null");
        log.info("Validating mandatory ExecutionReport fields");
        fixTagValidator.assertTagPresent(message, ExecID.FIELD, "ExecID");
        orderStatusValidator.assertOrderIdPresent(message);
        fixTagValidator.assertTagPresent(message, ExecType.FIELD, "ExecType");
    }

    /**
     * Validates the ExecType (tag 150) field.
     *
     * @param message          the FIX message
     * @param expectedExecType the expected execution type char (e.g. ExecType.NEW)
     */
    public void assertExecType(Message message, char expectedExecType) {
        log.info("Validating ExecType(150): expected='{}'", expectedExecType);
        fixTagValidator.assertTagEquals(message, ExecType.FIELD, expectedExecType, "ExecType");
    }

    /**
     * Validates the ExecType (tag 150) by its human-readable string label.
     *
     * @param message          the FIX message
     * @param expectedExecType human-readable exec type (e.g. "NEW", "FILL", "CANCELED")
     */
    public void assertExecType(Message message, String expectedExecType) {
        char execTypeChar = resolveExecType(expectedExecType);
        assertExecType(message, execTypeChar);
    }

    /**
     * Validates the OrderQty (tag 38) field.
     *
     * @param message       the FIX message
     * @param expectedQty   the expected order quantity
     */
    public void assertOrderQty(Message message, double expectedQty) {
        log.info("Validating OrderQty(38): expected='{}'", expectedQty);
        fixTagValidator.assertTagEquals(message, OrderQty.FIELD,
                String.valueOf((int) expectedQty), "OrderQty");
    }

    /**
     * Validates the AvgPx (tag 6) field.
     *
     * @param message      the FIX message
     * @param expectedAvgPx the expected average price
     */
    public void assertAvgPx(Message message, double expectedAvgPx) {
        log.info("Validating AvgPx(6): expected='{}'", expectedAvgPx);
        fixTagValidator.assertTagEquals(message, AvgPx.FIELD,
                String.valueOf(expectedAvgPx), "AvgPx");
    }

    /**
     * Validates the LeavesQty (tag 151) field.
     *
     * @param message           the FIX message
     * @param expectedLeavesQty the expected leaves quantity
     */
    public void assertLeavesQty(Message message, double expectedLeavesQty) {
        log.info("Validating LeavesQty(151): expected='{}'", expectedLeavesQty);
        fixTagValidator.assertTagEquals(message, LeavesQty.FIELD,
                String.valueOf((int) expectedLeavesQty), "LeavesQty");
    }

    /**
     * Validates the CumQty (tag 14) field.
     *
     * @param message        the FIX message
     * @param expectedCumQty the expected cumulative quantity
     */
    public void assertCumQty(Message message, double expectedCumQty) {
        log.info("Validating CumQty(14): expected='{}'", expectedCumQty);
        fixTagValidator.assertTagEquals(message, CumQty.FIELD,
                String.valueOf((int) expectedCumQty), "CumQty");
    }

    /**
     * Validates the Text (tag 58) field contains the expected value.
     *
     * @param message      the FIX message
     * @param expectedText the expected text
     */
    public void assertText(Message message, String expectedText) {
        log.info("Validating Text(58): expected='{}'", expectedText);
        fixTagValidator.assertTagEquals(message, Text.FIELD, expectedText, "Text");
    }

    /**
     * Validates that the Text (tag 58) field is present.
     *
     * @param message the FIX message
     */
    public void assertTextPresent(Message message) {
        fixTagValidator.assertTagPresent(message, Text.FIELD, "Text");
    }

    /**
     * Validates full ExecutionReport for a NEW order.
     *
     * @param message   the FIX message
     * @param clOrdId   expected ClOrdID
     * @param symbol    expected symbol
     * @param quantity  expected order quantity
     */
    public void assertNewOrderAck(Message message, String clOrdId, String symbol, double quantity) {
        log.info("Validating NewOrder ExecutionReport: clOrdId={}, symbol={}, qty={}", clOrdId, symbol, quantity);
        assertMandatoryFields(message);
        orderStatusValidator.assertClOrdId(message, clOrdId);
        orderStatusValidator.assertSymbol(message, symbol);
        orderStatusValidator.assertOrdStatus(message, "NEW");
        assertExecType(message, ExecType.NEW);
        assertOrderQty(message, quantity);
    }

    /**
     * Validates ExecutionReport for a FILLED order.
     *
     * @param message  the FIX message
     * @param clOrdId  expected ClOrdID
     * @param symbol   expected symbol
     * @param quantity expected order quantity
     */
    public void assertFillExecutionReport(Message message, String clOrdId, String symbol, double quantity) {
        log.info("Validating Fill ExecutionReport: clOrdId={}, symbol={}, qty={}", clOrdId, symbol, quantity);
        assertMandatoryFields(message);
        orderStatusValidator.assertClOrdId(message, clOrdId);
        orderStatusValidator.assertSymbol(message, symbol);
        orderStatusValidator.assertOrdStatus(message, "FILLED");
        assertExecType(message, ExecType.FILL);
        assertOrderQty(message, quantity);
        assertCumQty(message, quantity);
        assertLeavesQty(message, 0);
    }

    /**
     * Resolves a human-readable ExecType string to its FIX char value.
     *
     * @param execType the human-readable exec type string
     * @return the FIX ExecType char
     */
    private char resolveExecType(String execType) {
        return switch (execType.toUpperCase()) {
            case "NEW"              -> ExecType.NEW;
            case "PARTIAL_FILL",
                 "PARTIAL"         -> ExecType.PARTIAL_FILL;
            case "FILL"            -> ExecType.FILL;
            case "DONE_FOR_DAY"    -> ExecType.DONE_FOR_DAY;
            case "CANCELED", "CANCEL" -> ExecType.CANCELED;
            case "REPLACED", "REPLACE" -> ExecType.REPLACED;
            case "PENDING_CANCEL"  -> ExecType.PENDING_CANCEL;
            case "STOPPED"         -> ExecType.STOPPED;
            case "REJECTED", "REJECT" -> ExecType.REJECTED;
            case "SUSPENDED"       -> ExecType.SUSPENDED;
            case "PENDING_NEW"     -> ExecType.PENDING_NEW;
            case "CALCULATED"      -> ExecType.CALCULATED;
            case "EXPIRED"         -> ExecType.EXPIRED;
            case "PENDING_REPLACE" -> ExecType.PENDING_REPLACE;
            case "TRADE"           -> ExecType.TRADE;
            case "TRADE_CORRECT"   -> ExecType.TRADE_CORRECT;
            case "TRADE_CANCEL"    -> ExecType.TRADE_CANCEL;
            case "ORDER_STATUS"    -> ExecType.ORDER_STATUS;
            default -> throw new IllegalArgumentException("Unknown ExecType: " + execType);
        };
    }
}
