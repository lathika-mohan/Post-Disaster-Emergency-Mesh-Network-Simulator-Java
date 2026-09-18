package com.meshsim.energy;

import com.meshsim.model.Node;

/**
 * First-order radio energy model. drain() is called from multiple node
 * threads concurrently (Phase 6); this is a small, low-contention critical
 * section, so plain `synchronized` is used here deliberately, in contrast to
 * the ReentrantReadWriteLock guarding the much hotter, coarser-grained graph
 * in MeshNetwork. Different contention shapes, different tools.
 */
public final class EnergyModel {

    public static final double TRANSMIT_COST_BASE = 0.01;
    public static final double RECEIVE_COST = 0.004;
    public static final double IDLE_DRAIN = 0.0005;
    public static final double SLEEP_DRAIN = 0.00005;

    public synchronized void drain(Node n, double distanceMetres) {
        double distanceFactor = Math.pow(Math.max(1.0, distanceMetres), 2) / 100.0;
        n.drain(TRANSMIT_COST_BASE * distanceFactor);
    }

    public synchronized void onReceive(Node n) {
        n.drain(RECEIVE_COST);
    }

    public synchronized void onIdleTick(Node n) {
        n.drain(IDLE_DRAIN);
    }

    public synchronized void onSleepTick(Node n) {
        n.drain(SLEEP_DRAIN);
    }
}
