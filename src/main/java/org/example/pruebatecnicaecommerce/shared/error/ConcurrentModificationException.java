package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when a concurrent modification conflict occurs
 */
public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}