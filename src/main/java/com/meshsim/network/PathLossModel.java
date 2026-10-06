package com.meshsim.network;

import com.meshsim.energy.RadioProfile;
import com.meshsim.model.Node;
import com.meshsim.model.Obstacle;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;

import java.util.Random;

/**
 * Simplified log-distance path loss model with optional Gaussian shadowing.
 */
public final class PathLossModel {

    public static final double REFERENCE_DISTANCE_M = 1.0;
    public static final double REFERENCE_LOSS_DB = 40.0;
    public static final double OPEN_GROUND_EXPONENT = 2.0;
    public static final double SHADOWING_STD_DEV_DB = 4.0;
    public static final double RECEIVER_SENSITIVITY_DB = RadioProfile.STANDARD_HANDHELD.sensitivityDb();
    public static final double TRANSMIT_POWER_DB = RadioProfile.STANDARD_HANDHELD.transmitPowerDb();
    public static final double MAX_RANGE_M = RadioProfile.STANDARD_HANDHELD.rangeMetres();

    private static Random random = new Random(42);
    private static boolean shadowingEnabled = false;

    private PathLossModel() {
    }

    public static synchronized void setSeed(long seed) {
        random = new Random(seed);
    }

    public static synchronized void setShadowingEnabled(boolean enabled) {
        shadowingEnabled = enabled;
    }

    public static synchronized boolean isShadowingEnabled() {
        return shadowingEnabled;
    }

    public static double computeQuality(Node a, Node b, Scenario scenario) {
        return computeQuality(a, b, scenario, OPEN_GROUND_EXPONENT);
    }

    public static double computeQuality(Node a, Node b, Scenario scenario, double terrainExponent) {
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
                + 10 * terrainExponent * Math.log10(distance / REFERENCE_DISTANCE_M);
        double receivedPowerDb = TRANSMIT_POWER_DB - pathLossDb + 20 * Math.log10(attenuation + 0.001);

        if (shadowingEnabled) {
            synchronized (PathLossModel.class) {
                receivedPowerDb += random.nextGaussian() * SHADOWING_STD_DEV_DB;
            }
        }

        if (receivedPowerDb < RECEIVER_SENSITIVITY_DB) {
            return 0.0;
        }
        double margin = receivedPowerDb - RECEIVER_SENSITIVITY_DB;
        return Math.max(0.0, Math.min(1.0, margin / 40.0));
    }
}
