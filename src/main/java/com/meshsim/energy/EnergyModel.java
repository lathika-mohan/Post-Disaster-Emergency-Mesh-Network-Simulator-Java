package com.meshsim.energy;

/**
 * Simplified radio energy model (inspired by the classic first-order model
 * used in wireless sensor network research).
 *
 * E_tx(k,d) = k * E_elec + k * E_amp * d^n     (transmit k bits over distance d)
 * E_rx(k)   = k * E_elec                        (receive k bits)
 */
public class EnergyModel {
    private static final double E_ELEC = 50e-9;   // Joules per bit, electronics cost
    private static final double E_AMP = 100e-12;   // Joules per bit per distance^n, amplifier cost
    private static final int PACKET_BITS = 2048;   // default emergency-message packet size

    /** Transmission energy for one packet over the given distance, at path-loss exponent n. */
    public double transmitEnergy(double distance, double pathLossExponent) {
        return PACKET_BITS * E_ELEC + PACKET_BITS * E_AMP * Math.pow(Math.max(distance, 1), pathLossExponent);
    }

    /** Reception energy for one packet (distance-independent). */
    public double receiveEnergy() {
        return PACKET_BITS * E_ELEC;
    }

    public int packetBits() { return PACKET_BITS; }
}
