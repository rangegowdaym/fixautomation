package com.fixautomation.fixclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MsgType;

/**
 * QuickFIX/J Application implementation.
 * Handles all FIX session and message lifecycle events.
 */
@Component
public class FixApplication implements Application {

    private static final Logger log = LogManager.getLogger(FixApplication.class);

    private final FixMessageListener messageListener;

    @Autowired
    public FixApplication(FixMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("FIX session created: {}", sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("FIX session logged on: {}", sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("FIX session logged out: {}", sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            log.debug("Sending admin message type={} to session={}", msgType, sessionID);
        } catch (FieldNotFound e) {
            log.warn("Could not determine admin message type: {}", e.getMessage());
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            log.debug("Received admin message type={} from session={}", msgType, sessionID);
        } catch (FieldNotFound e) {
            log.warn("Could not determine admin message type: {}", e.getMessage());
        }
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);
            log.info("Sending app message type={} to session={}: {}", msgType, sessionID, message);
        } catch (FieldNotFound e) {
            log.warn("Could not determine app message type: {}", e.getMessage());
        }
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.info("Received app message from session={}: {}", sessionID, message);
        messageListener.onMessage(message, sessionID);
    }
}
