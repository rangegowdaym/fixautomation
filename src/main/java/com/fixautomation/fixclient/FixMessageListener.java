package com.fixautomation.fixclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.OrderCancelReject;

/**
 * Routes incoming FIX messages to the appropriate handler.
 * Dispatches by MsgType and delegates to {@link FixResponseStore}.
 */
@Component
public class FixMessageListener {

    private static final Logger log = LogManager.getLogger(FixMessageListener.class);

    private final FixResponseStore fixResponseStore;

    @Autowired
    public FixMessageListener(FixResponseStore fixResponseStore) {
        this.fixResponseStore = fixResponseStore;
    }

    /**
     * Handles an inbound FIX message from the engine.
     *
     * @param message   the received FIX message
     * @param sessionID the session that received the message
     */
    public void onMessage(Message message, SessionID sessionID) {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            log.info("Received FIX message type={} from session={}", msgType, sessionID);

            switch (msgType) {
                case ExecutionReport.MSGTYPE -> handleExecutionReport(message, sessionID);
                case OrderCancelReject.MSGTYPE -> handleOrderCancelReject(message, sessionID);
                case BusinessMessageReject.MSGTYPE -> handleBusinessMessageReject(message, sessionID);
                default -> {
                    log.warn("Unhandled FIX message type={}: {}", msgType, message);
                    fixResponseStore.storeMessage(message);
                }
            }
        } catch (FieldNotFound e) {
            log.error("Failed to determine message type for message={}: {}", message, e.getMessage(), e);
        }
    }

    private void handleExecutionReport(Message message, SessionID sessionID) {
        try {
            String clOrdId = message.getString(ClOrdID.FIELD);
            log.info("Handling ExecutionReport: ClOrdID={}, session={}", clOrdId, sessionID);
            fixResponseStore.storeResponse(clOrdId, message);
        } catch (FieldNotFound e) {
            log.warn("ExecutionReport missing ClOrdID, storing without key: {}", e.getMessage());
            fixResponseStore.storeMessage(message);
        }
    }

    private void handleOrderCancelReject(Message message, SessionID sessionID) {
        try {
            String clOrdId = message.getString(ClOrdID.FIELD);
            log.info("Handling OrderCancelReject: ClOrdID={}, session={}", clOrdId, sessionID);
            fixResponseStore.storeResponse(clOrdId, message);
        } catch (FieldNotFound e) {
            log.warn("OrderCancelReject missing ClOrdID: {}", e.getMessage());
            fixResponseStore.storeMessage(message);
        }
    }

    private void handleBusinessMessageReject(Message message, SessionID sessionID) {
        log.warn("Handling BusinessMessageReject from session={}: {}", sessionID, message);
        fixResponseStore.storeMessage(message);
    }
}
