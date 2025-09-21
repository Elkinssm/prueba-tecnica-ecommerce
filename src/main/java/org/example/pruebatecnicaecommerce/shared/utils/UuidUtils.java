package org.example.pruebatecnicaecommerce.shared.utils;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for UUID operations and validations
 */
public final class UuidUtils {

    public static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private UuidUtils() {
    }

    /**
     * Validates if a string is a valid UUID format
     */
    public static boolean isValidUuid(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }

    /**
     * Safely converts a string to UUID
     * 
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static UUID fromString(String uuid) {
        if (!isValidUuid(uuid)) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }
        return UUID.fromString(uuid);
    }

    /**
     * Generates a new random UUID
     */
    public static UUID randomUuid() {
        return UUID.randomUUID();
    }
}