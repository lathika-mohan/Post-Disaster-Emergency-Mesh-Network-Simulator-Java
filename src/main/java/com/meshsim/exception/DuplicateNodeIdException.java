package com.meshsim.exception;

/** Unchecked: two entities were registered under the same id. This is always a bug. */
public class DuplicateNodeIdException extends InvalidTopologyException {
    public DuplicateNodeIdException(String id) {
        super("Duplicate id: " + id);
    }
}
