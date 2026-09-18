package com.meshsim.model;

/** The kinds of node that can exist in a disaster-response mesh. */
public enum NodeType {
    SURVIVOR(1.0),
    RESCUE_TEAM(1.8),
    STATIC_RELAY(0.0),
    BASE_STATION(0.0);

    private final double speedFactor;

    NodeType(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    /** Relative movement speed multiplier used by the mobility engine. 0 = never moves. */
    public double speedFactor() {
        return speedFactor;
    }

    public boolean isMobile() {
        return speedFactor > 0.0;
    }
}
