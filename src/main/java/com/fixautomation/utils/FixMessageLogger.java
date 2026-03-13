package com.fixautomation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickfix.Message;

/**
 * Utility class for structured FIX message logging.
 * Logs FIX messages with their type label for easier tracing.
 */
public class FixMessageLogger {

    private static final Logger log = LogManager.getLogger(FixMessageLogger.class);

    private FixMessageLogger() {
    }

    /**
     * Logs a FIX message with its type label.
     *
     * @param message   the FIX message
     * @param msgType   the message type label (e.g. "NewOrderSingle")
     */
    public static void log(Message message, String msgType) {
        if (log.isInfoEnabled()) {
            log.info("[FIX OUT] {}:{}", msgType, message);
        }
    }

    /**
     * Logs a received FIX message with its type label.
     *
     * @param message   the FIX message
     * @param msgType   the message type label (e.g. "ExecutionReport")
     */
    public static void logReceived(Message message, String msgType) {
        if (log.isInfoEnabled()) {
            log.info("[FIX IN]  {}:{}", msgType, message);
        }
    }

    /**
     * Returns the raw FIX message string.
     *
     * @param message the FIX message
     * @return the raw message string
     */
    public static String toRawString(Message message) {
        return message != null ? message.toString() : "<null>";
    }
}
