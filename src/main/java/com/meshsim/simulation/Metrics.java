package com.meshsim.simulation;

import java.util.ArrayList;
import java.util.List;

/** Aggregates the performance metrics defined in the project's design document. */
public class Metrics {
    private int packetsSent = 0;
    private int packetsDelivered = 0;
    private final List<Integer> hopCounts = new ArrayList<>();
    private double energyConsumedJoules = 0;
    private Integer networkLifetimeTick = null; // simulation tick of first node failure
    private final List<Long> recoveryTimesMs = new ArrayList<>();

    public void recordAttempt() { packetsSent++; }

    public void recordDelivery(int hopCount) {
        packetsDelivered++;
        hopCounts.add(hopCount);
    }

    public void recordEnergyUse(double joules) { energyConsumedJoules += joules; }

    public void recordFirstFailure(int tick) {
        if (networkLifetimeTick == null) networkLifetimeTick = tick;
    }

    public void recordRecoveryTime(long millis) { recoveryTimesMs.add(millis); }

    public double packetDeliveryRatio() {
        return packetsSent == 0 ? 0 : (double) packetsDelivered / packetsSent * 100.0;
    }

    public double averageHopCount() {
        return hopCounts.isEmpty() ? 0 : hopCounts.stream().mapToInt(i -> i).average().orElse(0);
    }

    public double energyEfficiency() {
        return energyConsumedJoules == 0 ? 0 : packetsDelivered / energyConsumedJoules;
    }

    public Integer getNetworkLifetimeTick() { return networkLifetimeTick; }
    public int getPacketsSent() { return packetsSent; }
    public int getPacketsDelivered() { return packetsDelivered; }
    public double getEnergyConsumedJoules() { return energyConsumedJoules; }

    public double averageRecoveryTimeMs() {
        return recoveryTimesMs.isEmpty() ? 0 : recoveryTimesMs.stream().mapToLong(l -> l).average().orElse(0);
    }

    public void printSummary() {
        System.out.println("---- Performance Metrics ----");
        System.out.printf("Packets sent            : %d%n", packetsSent);
        System.out.printf("Packets delivered        : %d%n", packetsDelivered);
        System.out.printf("Packet Delivery Ratio    : %.1f%%%n", packetDeliveryRatio());
        System.out.printf("Average Hop Count        : %.2f%n", averageHopCount());
        System.out.printf("Energy Consumed          : %.6f J%n", energyConsumedJoules);
        System.out.printf("Energy Efficiency        : %.2f deliveries/J%n", energyEfficiency());
        System.out.printf("Network Lifetime (tick)  : %s%n", networkLifetimeTick == null ? "no failures yet" : networkLifetimeTick.toString());
        System.out.printf("Avg Route Recovery Time  : %.1f ms%n", averageRecoveryTimeMs());
    }
}
