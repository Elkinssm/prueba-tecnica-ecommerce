package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when a user already exists
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String identifier) {
        super("User already exists: " + identifier);
    }
}