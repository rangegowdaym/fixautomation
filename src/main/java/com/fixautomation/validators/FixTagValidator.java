package com.fixautomation.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import quickfix.FieldNotFound;
import quickfix.Message;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Validates individual FIX tags on a received FIX message.
 */
@Component
public class FixTagValidator {

    private static final Logger log = LogManager.getLogger(FixTagValidator.class);

    /**
     * Asserts that the FIX message contains the expected string value for the given tag.
     *
     * @param message      the FIX message
     * @param tagNumber    the FIX tag number
     * @param expectedValue the expected string value
     * @param tagName      human-readable tag name for logging
     */
    public void assertTagEquals(Message message, int tagNumber, String expectedValue, String tagName) {
        assertNotNull(message, "FIX message must not be null when validating tag " + tagName);
        try {
            String actualValue = message.getString(tagNumber);
            log.info("Validating FIX tag {}({}): expected='{}', actual='{}'",
                    tagName, tagNumber, expectedValue, actualValue);
            assertEquals(actualValue, expectedValue,
                    String.format("FIX tag %s(%d) mismatch", tagName, tagNumber));
        } catch (FieldNotFound e) {
            throw new AssertionError(
                    String.format("FIX tag %s(%d) not found in message: %s", tagName, tagNumber, message), e);
        }
    }

    /**
     * Asserts that the FIX message contains the expected char value for the given tag.
     *
     * @param message      the FIX message
     * @param tagNumber    the FIX tag number
     * @param expectedValue the expected char value
     * @param tagName      human-readable tag name for logging
     */
    public void assertTagEquals(Message message, int tagNumber, char expectedValue, String tagName) {
        assertTagEquals(message, tagNumber, String.valueOf(expectedValue), tagName);
    }

    /**
     * Asserts that a FIX tag is present (non-null, non-empty) in the message.
     *
     * @param message   the FIX message
     * @param tagNumber the FIX tag number
     * @param tagName   human-readable tag name for logging
     */
    public void assertTagPresent(Message message, int tagNumber, String tagName) {
        assertNotNull(message, "FIX message must not be null when validating tag " + tagName);
        try {
            String value = message.getString(tagNumber);
            assertTrue(value != null && !value.isBlank(),
                    String.format("FIX tag %s(%d) is empty or missing", tagName, tagNumber));
            log.info("FIX tag {}({}) is present: '{}'", tagName, tagNumber, value);
        } catch (FieldNotFound e) {
            throw new AssertionError(
                    String.format("FIX tag %s(%d) not found in message: %s", tagName, tagNumber, message), e);
        }
    }

    /**
     * Returns the string value of a FIX tag, or throws AssertionError if not found.
     *
     * @param message   the FIX message
     * @param tagNumber the FIX tag number
     * @param tagName   human-readable tag name for logging
     * @return the tag value as a string
     */
    public String getTagValue(Message message, int tagNumber, String tagName) {
        assertNotNull(message, "FIX message must not be null when reading tag " + tagName);
        try {
            return message.getString(tagNumber);
        } catch (FieldNotFound e) {
            throw new AssertionError(
                    String.format("FIX tag %s(%d) not found in message: %s", tagName, tagNumber, message), e);
        }
    }
}
