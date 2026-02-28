package com.fixautomation.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Reusable JSON validation utility.
 * Uses Jackson for proper JSON parsing instead of string contains().
 */
public class JsonValidator {

    private static final Logger log = LoggerFactory.getLogger(JsonValidator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonValidator() {
    }

    /**
     * Validates that a JSON string contains a field with the expected value.
     */
    public static void assertFieldEquals(String json, String fieldPath, String expectedValue) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = navigatePath(root, fieldPath);
            String actualValue = node.asText();
            if (!Objects.equals(actualValue, expectedValue)) {
                throw new AssertionError(String.format(
                        "JSON field '%s' mismatch: expected='%s', actual='%s'",
                        fieldPath, expectedValue, actualValue));
            }
            log.debug("Validated JSON field '{}' = '{}'", fieldPath, expectedValue);
        } catch (IOException e) {
            throw new AssertionError("Failed to parse JSON for validation: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that a JSON string has a non-null, non-empty field.
     */
    public static void assertFieldPresent(String json, String fieldPath) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = navigatePath(root, fieldPath);
            if (node.isNull() || node.isMissingNode() || node.asText().isBlank()) {
                throw new AssertionError(String.format(
                        "JSON field '%s' is missing or empty", fieldPath));
            }
            log.debug("Validated JSON field '{}' is present with value '{}'", fieldPath, node.asText());
        } catch (IOException e) {
            throw new AssertionError("Failed to parse JSON for validation: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a string value from a JSON path (supports dot-notation).
     */
    public static String extractField(String json, String fieldPath) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = navigatePath(root, fieldPath);
            if (node.isMissingNode() || node.isNull()) {
                throw new IllegalArgumentException("Field not found: " + fieldPath);
            }
            return node.asText();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the JSON is structurally valid.
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static JsonNode navigatePath(JsonNode root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            current = current.path(part);
        }
        return current;
    }
}
