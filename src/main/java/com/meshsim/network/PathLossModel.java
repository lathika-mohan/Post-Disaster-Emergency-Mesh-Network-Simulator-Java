package com.meshsim.network;

import com.meshsim.model.Node;
import com.meshsim.model.Obstacle;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;

/**
 * Simplified log-distance path loss model.
 * Unit 1 "static members": named constants replace magic numbers.
 */
public final class PathLossModel {

    public static final double REFERENCE_DISTANCE_M = 1.0;
    public static final double REFERENCE_LOSS_DB = 40.0;
    public static final double OPEN_GROUND_EXPONENT = 2.0;
    public static final double RECEIVER_SENSITIVITY_DB = -90.0;
    public static final double TRANSMIT_POWER_DB = 20.0;
    public static final double MAX_RANGE_M = 60.0;

    private PathLossModel() {
    }

    /**
     * Returns link quality in [0,1], or 0.0 if no link exists (out of range or
     * an obstacle fully blocks line of sight).
     */
    public static double computeQuality(Node a, Node b, Scenario scenario) {
        Point pa = a.position();
        Point pb = b.position();
        double distance = pa.distanceTo(pb);
        if (distance > MAX_RANGE_M || distance == 0) {
            return distance == 0 ? 1.0 : 0.0;
        }

        double attenuation = 1.0;
        for (Obstacle o : scenario.obstacles()) {
            if (o.blocks(pa, pb)) {
                attenuation *= o.attenuationFactor();
            }
        }
        if (attenuation <= 0.001) {
            return 0.0; // fully blocked
        }

        double pathLossDb = REFERENCE_LOSS_DB
                + 10 * OPEN_GROUND_EXPONENT * Math.log10(distance / REFERENCE_DISTANCE_M);
        double receivedPowerDb = TRANSMIT_POWER_DB - pathLossDb + 20 * Math.log10(attenuation + 0.001);

        if (receivedPowerDb < RECEIVER_SENSITIVITY_DB) {
            return 0.0;
        }
        double margin = receivedPowerDb - RECEIVER_SENSITIVITY_DB;
        return Math.max(0.0, Math.min(1.0, margin / 40.0));
    }
}
