package com.meshsim.exception;

/**
 * Root of the checked exception hierarchy: failures that are a legitimate,
 * recoverable simulation outcome caused by the environment (a missing route,
 * a partitioned network, a bad input file) rather than a programming bug.
 */
public class MeshSimulationException extends Exception {
    public MeshSimulationException(String message) {
        super(message);
    }

    public MeshSimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
