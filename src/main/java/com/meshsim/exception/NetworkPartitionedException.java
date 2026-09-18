package com.meshsim.exception;

/** Thrown when the network graph has split into two or more disconnected components. */
public class NetworkPartitionedException extends MeshSimulationException {

    private final int componentASize;
    private final int componentBSize;

    public NetworkPartitionedException(int componentASize, int componentBSize) {
        super("Network partitioned into components of size " + componentASize
                + " and " + componentBSize);
        this.componentASize = componentASize;
        this.componentBSize = componentBSize;
    }

    public int componentASize() {
        return componentASize;
    }

    public int componentBSize() {
        return componentBSize;
    }
}
