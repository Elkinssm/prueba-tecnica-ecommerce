package org.example.pruebatecnicaecommerce.shared.error;

/**
 * Exception thrown when an invalid order state transition is attempted
 */
public class InvalidOrderStateTransitionException extends DomainException {

    public InvalidOrderStateTransitionException(String currentState, String targetState) {
        super(String.format("Invalid order state transition from %s to %s", currentState, targetState));
    }
}