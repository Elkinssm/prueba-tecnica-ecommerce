package org.example.pruebatecnicaecommerce.shared.error;

import lombok.Value;

import java.time.Instant;

/**
 * Standard error response DTO
 */
@Value
public class ErrorResponse {
    String code;
    String message;
    Instant timestamp;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = Instant.now();
    }
}