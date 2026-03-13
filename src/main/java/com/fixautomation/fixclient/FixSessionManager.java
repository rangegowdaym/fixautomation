package com.fixautomation.fixclient;

import com.fixautomation.config.FixConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the QuickFIX/J initiator lifecycle.
 * Reads session configuration, starts/stops the FIX engine,
 * and provides a send API for FIX messages.
 */
@Component
public class FixSessionManager {

    private static final Logger log = LogManager.getLogger(FixSessionManager.class);

    private final FixApplication fixApplication;
    private final FixConfig fixConfig;

    private Initiator initiator;
    private volatile boolean connected = false;

    @Autowired
    public FixSessionManager(FixApplication fixApplication, FixConfig fixConfig) {
        this.fixApplication = fixApplication;
        this.fixConfig = fixConfig;
    }

    /**
     * Starts the FIX initiator if FIX is enabled in configuration.
     * Called automatically after Spring context initialization.
     */
    @PostConstruct
    public void start() {
        if (!fixConfig.isEnabled()) {
            log.info("FIX client is disabled via configuration — skipping initiator startup");
            return;
        }
        try {
            SessionSettings settings = loadSessionSettings();
            FileStoreFactory storeFactory = new FileStoreFactory(settings);
            DefaultMessageFactory messageFactory = new DefaultMessageFactory();
            initiator = new SocketInitiator(fixApplication, storeFactory, settings, messageFactory);
            initiator.start();
            connected = true;
            log.info("FIX initiator started successfully");
        } catch (ConfigError e) {
            log.error("Failed to start FIX initiator: {}", e.getMessage(), e);
            throw new IllegalStateException("FIX initiator startup failed", e);
        }
    }

    /**
     * Stops the FIX initiator gracefully.
     * Called automatically before Spring context destruction.
     */
    @PreDestroy
    public void stop() {
        if (initiator != null && connected) {
            log.info("Stopping FIX initiator...");
            initiator.stop(true);
            connected = false;
            log.info("FIX initiator stopped");
        }
    }

    /**
     * Sends a FIX message to the engine on the first active session.
     *
     * @param message the FIX message to send
     * @throws SessionNotFound if no active session is available
     * @throws IllegalStateException if the FIX client is not connected
     */
    public void send(Message message) throws SessionNotFound {
        if (!connected || initiator == null) {
            throw new IllegalStateException("FIX client is not connected");
        }
        List<SessionID> sessions = getSessions();
        if (sessions.isEmpty()) {
            throw new IllegalStateException("No active FIX sessions available");
        }
        SessionID sessionID = sessions.get(0);
        log.info("Sending FIX message on session={}: {}", sessionID, message);
        Session.sendToTarget(message, sessionID);
    }

    /**
     * Sends a FIX message to a specific session.
     *
     * @param message   the FIX message to send
     * @param sessionID the target session ID
     * @throws SessionNotFound if the session is not found
     */
    public void send(Message message, SessionID sessionID) throws SessionNotFound {
        if (!connected || initiator == null) {
            throw new IllegalStateException("FIX client is not connected");
        }
        log.info("Sending FIX message on session={}: {}", sessionID, message);
        Session.sendToTarget(message, sessionID);
    }

    /**
     * Returns all sessions managed by the initiator.
     *
     * @return list of session IDs
     */
    public List<SessionID> getSessions() {
        if (initiator == null) {
            return List.of();
        }
        return new ArrayList<>(initiator.getSessions());
    }

    /**
     * Returns true if the initiator is running and connected.
     *
     * @return connection status
     */
    public boolean isConnected() {
        return connected;
    }

    private SessionSettings loadSessionSettings() throws ConfigError {
        String configFile = fixConfig.getConfigFile();
        log.info("Loading FIX session settings from: {}", configFile);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
        if (inputStream == null) {
            throw new ConfigError("FIX configuration file not found: " + configFile);
        }
        return new SessionSettings(inputStream);
    }
}
