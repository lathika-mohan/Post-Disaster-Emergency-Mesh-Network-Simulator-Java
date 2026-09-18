package com.meshsim.model;

/**
 * Immutable, thread-safe hand-off of a Node's state to the GUI or database.
 * Node itself stays mutable (battery drains, position changes under
 * concurrent access), so callers that need a stable read take a snapshot.
 */
public record NodeSnapshot(String id, NodeType type, Point position, double energy, boolean alive) {
}
