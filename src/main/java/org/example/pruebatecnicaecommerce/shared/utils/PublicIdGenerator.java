package org.example.pruebatecnicaecommerce.shared.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for generating user-friendly public IDs
 */
public final class PublicIdGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private PublicIdGenerator() {
        // Utility class
    }

    /**
     * Generates a user-friendly order ID
     * Format: ORD-YYYYMMDD-XXXX
     * Example: ORD-20250921-A1B2
     */
    public static String generateOrderId() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String random = generateRandomAlphanumeric(4);
        return String.format("ORD-%s-%s", date, random);
    }

    /**
     * Generates a user-friendly inventory ID
     * Format: INV-YYYYMMDD-XXXX
     */
    public static String generateInventoryId() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String random = generateRandomAlphanumeric(4);
        return String.format("INV-%s-%s", date, random);
    }

    private static String generateRandomAlphanumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return result.toString();
    }
}