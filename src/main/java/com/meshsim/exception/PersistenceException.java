package com.meshsim.exception;

/** Wraps a checked SQLException so persistence callers deal with one exception type. */
public class PersistenceException extends MeshSimulationException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
