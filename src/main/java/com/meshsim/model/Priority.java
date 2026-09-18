package com.meshsim.model;

/** Message priority, ordered from most to least urgent (Unit 4 PriorityQueue comparator target). */
public enum Priority {
    MEDICAL_EMERGENCY(0),
    TRAPPED_PERSON(1),
    STATUS_PING(2);

    private final int rank;

    Priority(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
