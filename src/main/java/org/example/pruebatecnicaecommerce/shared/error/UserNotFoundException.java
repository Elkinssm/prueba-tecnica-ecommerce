package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}