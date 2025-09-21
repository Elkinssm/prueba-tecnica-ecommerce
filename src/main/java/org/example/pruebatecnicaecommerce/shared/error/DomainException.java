package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Base exception for all domain-related exceptions
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}