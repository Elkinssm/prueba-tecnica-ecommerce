package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when an optimistic lock conflict occurs
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}