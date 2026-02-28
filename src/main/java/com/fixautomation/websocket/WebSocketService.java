package com.fixautomation.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixautomation.config.AppConfig;
import com.fixautomation.model.AckResponse;
import com.fixautomation.model.OrderRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WebSocket service for sending order requests and waiting for ACK responses.
 * Each call to sendAndWaitForAck() is thread-safe and uses a dedicated WebSocket connection.
 */
@Component
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private final AppConfig appConfig;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketService(AppConfig appConfig, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        this.appConfig = appConfig;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes the OrderRequest, sends it over a WebSocket connection,
     * and waits for the ACK response.
     *
     * @param orderRequest the order to send
     * @return the parsed AckResponse
     * @throws IOException if serialization or transport fails
     * @throws InterruptedException if the latch wait is interrupted
     */
    public AckResponse sendAndWaitForAck(OrderRequest orderRequest) throws IOException, InterruptedException {
        String requestJson = objectMapper.writeValueAsString(orderRequest);
        log.info("Sending order request via WebSocket: correlationId={}, payload={}",
                orderRequest.getCorrelationId(), requestJson);

        CountDownLatch ackLatch = new CountDownLatch(1);
        AtomicReference<String> ackMessageRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
                log.info("WebSocket connection opened");
                webSocket.send(requestJson);
                log.info("Order request sent, awaiting ACK...");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                log.info("ACK received: {}", text);
                ackMessageRef.set(text);
                ackLatch.countDown();
                webSocket.close(1000, "ACK received");
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t,
                                  @Nullable okhttp3.Response response) {
                log.error("WebSocket failure: {}", t.getMessage(), t);
                errorRef.set(t);
                ackLatch.countDown();
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                log.info("WebSocket closed: code={}, reason={}", code, reason);
            }
        };

        WebSocket webSocket = connect(listener);
        try {
            boolean timedOut = !ackLatch.await(appConfig.getWebsocketAckTimeoutSeconds(), TimeUnit.SECONDS);
            if (timedOut) {
                webSocket.close(1000, "Timeout waiting for ACK");
                throw new AssertionError(
                        "Timed out after " + appConfig.getWebsocketAckTimeoutSeconds() + "s waiting for WebSocket ACK");
            }
            if (errorRef.get() != null) {
                throw new IOException("WebSocket error: " + errorRef.get().getMessage(), errorRef.get());
            }
        } finally {
            webSocket.cancel();
        }

        String ackJson = ackMessageRef.get();
        AckResponse ack = objectMapper.readValue(ackJson, AckResponse.class);
        log.info("ACK parsed: correlationId={}, status={}", ack.getCorrelationId(), ack.getStatus());
        return ack;
    }

    /**
     * Creates and connects a WebSocket with the given listener.
     */
    public WebSocket connect(WebSocketListener listener) {
        Request request = new Request.Builder()
                .url(appConfig.getWebsocketUrl())
                .build();
        log.info("Connecting to WebSocket at {}", appConfig.getWebsocketUrl());
        return okHttpClient.newWebSocket(request, listener);
    }
}
