package com.fixautomation.utils;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Reusable retry/polling utility using Awaitility.
 */
public class RetryUtils {

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    private RetryUtils() {
    }

    /**
     * Polls the supplier until it returns a non-null value or the timeout is reached.
     *
     * @param supplier            the supplier to poll
     * @param timeoutSeconds      maximum wait time
     * @param pollIntervalMillis  interval between polls
     * @param description         description for logging
     * @return the non-null result from the supplier
     * @throws AssertionError if timeout is reached before a result is available
     */
    public static <T> T pollUntilPresent(Supplier<T> supplier,
                                         long timeoutSeconds,
                                         long pollIntervalMillis,
                                         String description) {
        log.info("Polling for '{}' (timeout={}s, interval={}ms)", description, timeoutSeconds, pollIntervalMillis);
        try {
            Awaitility.await(description)
                    .atMost(timeoutSeconds, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofMillis(pollIntervalMillis))
                    .pollDelay(Duration.ofMillis(pollIntervalMillis))
                    .until(() -> supplier.get() != null);
        } catch (ConditionTimeoutException e) {
            throw new AssertionError(
                    String.format("Timed out after %ds waiting for '%s'", timeoutSeconds, description), e);
        }
        T result = supplier.get();
        log.info("Polling for '{}' succeeded", description);
        return result;
    }

    /**
     * Polls until the callable condition returns true or the timeout is reached.
     */
    public static void waitUntil(Callable<Boolean> condition,
                                  long timeoutSeconds,
                                  long pollIntervalMillis,
                                  String description) {
        log.info("Waiting for condition '{}' (timeout={}s)", description, timeoutSeconds);
        try {
            Awaitility.await(description)
                    .atMost(timeoutSeconds, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofMillis(pollIntervalMillis))
                    .until(condition);
        } catch (ConditionTimeoutException e) {
            throw new AssertionError(
                    String.format("Timed out after %ds waiting for condition '%s'", timeoutSeconds, description), e);
        }
        log.info("Condition '{}' satisfied", description);
    }
}
