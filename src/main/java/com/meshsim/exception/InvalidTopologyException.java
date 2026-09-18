package com.meshsim.exception;

/**
 * Unchecked: signals a programming mistake in how the topology was built,
 * not a recoverable runtime condition.
 */
public class InvalidTopologyException extends IllegalStateException {
    public InvalidTopologyException(String message) {
        super(message);
    }
}
