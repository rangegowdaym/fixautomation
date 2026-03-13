package com.fixautomation.fixclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import quickfix.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe in-memory store for FIX messages received from the engine.
 * Uses ConcurrentHashMap to support parallel test execution.
 */
@Component
public class FixResponseStore {

    private static final Logger log = LogManager.getLogger(FixResponseStore.class);

    /** Key: ClOrdID → latest response Message */
    private final Map<String, Message> responsesByClOrdId = new ConcurrentHashMap<>();

    /** All received messages in order (ExecutionReports, Rejects, etc.) */
    private final List<Message> allMessages = new CopyOnWriteArrayList<>();

    /**
     * Stores a received FIX message, indexed by ClOrdID (tag 11).
     *
     * @param clOrdId the client order ID
     * @param message the FIX message
     */
    public void storeResponse(String clOrdId, Message message) {
        responsesByClOrdId.put(clOrdId, message);
        allMessages.add(message);
        log.info("Stored FIX response for ClOrdID={}: {}", clOrdId, message);
    }

    /**
     * Stores a received FIX message without a ClOrdID (e.g. SessionReject).
     *
     * @param message the FIX message
     */
    public void storeMessage(Message message) {
        allMessages.add(message);
        log.info("Stored FIX message (no ClOrdID): {}", message);
    }

    /**
     * Retrieves the latest response for the given ClOrdID.
     *
     * @param clOrdId the client order ID
     * @return the FIX message, or null if not yet received
     */
    public Message getResponse(String clOrdId) {
        return responsesByClOrdId.get(clOrdId);
    }

    /**
     * Checks whether a response has been received for the given ClOrdID.
     *
     * @param clOrdId the client order ID
     * @return true if a response exists
     */
    public boolean hasResponse(String clOrdId) {
        return responsesByClOrdId.containsKey(clOrdId);
    }

    /**
     * Returns all received messages.
     *
     * @return unmodifiable view of all messages
     */
    public List<Message> getAllMessages() {
        return List.copyOf(allMessages);
    }

    /**
     * Clears all stored messages (useful between test scenarios).
     */
    public void clear() {
        responsesByClOrdId.clear();
        allMessages.clear();
        log.debug("FixResponseStore cleared");
    }

    /**
     * Removes the response for the given ClOrdID.
     *
     * @param clOrdId the client order ID
     */
    public void remove(String clOrdId) {
        responsesByClOrdId.remove(clOrdId);
    }
}
