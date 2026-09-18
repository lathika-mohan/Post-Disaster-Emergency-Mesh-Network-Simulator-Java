package com.meshsim.exception;

/** Thrown when a router cannot find any path from source to destination. */
public class NodeUnreachableException extends MeshSimulationException {

    private final String sourceId;
    private final String destId;
    private final int attemptedHops;

    public NodeUnreachableException(String sourceId, String destId) {
        this(sourceId, destId, 0);
    }

    public NodeUnreachableException(String sourceId, String destId, int attemptedHops) {
        super("No route found from " + sourceId + " to " + destId
                + " (attempted hops: " + attemptedHops + ")");
        this.sourceId = sourceId;
        this.destId = destId;
        this.attemptedHops = attemptedHops;
    }

    public String sourceId() {
        return sourceId;
    }

    public String destId() {
        return destId;
    }

    public int attemptedHops() {
        return attemptedHops;
    }
}
