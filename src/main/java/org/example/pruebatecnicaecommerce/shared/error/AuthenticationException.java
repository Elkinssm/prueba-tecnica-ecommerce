package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends DomainException {

    public AuthenticationException(String message) {
        super(message);
    }
}