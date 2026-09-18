package com.meshsim.energy;

/** Named radio characteristics per node class - kept as constants, not magic numbers. */
public record RadioProfile(double transmitPowerDb, double sensitivityDb, double rangeMetres) {

    public static final RadioProfile STANDARD_HANDHELD =
            new RadioProfile(20.0, -90.0, 60.0);
    public static final RadioProfile RESCUE_TEAM_RADIO =
            new RadioProfile(27.0, -95.0, 120.0);
    public static final RadioProfile FIXED_RELAY =
            new RadioProfile(30.0, -100.0, 200.0);
}
