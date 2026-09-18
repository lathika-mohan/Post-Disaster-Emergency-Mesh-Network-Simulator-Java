package com.meshsim.util;

import com.meshsim.model.Node;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Always-sorted battery leaderboard. Declared as SortedSet (Unit 5
 * "programming to the interface"), backed by TreeSet.
 * equals()/hashCode()/compareTo() on Node matter here: without a correct
 * compareTo a TreeSet silently drops or misorders entries.
 */
public final class BatteryLeaderboard {

    private final SortedSet<Node> byEnergyAscending =
            new TreeSet<>(Comparator.comparingDouble(Node::energy).thenComparing(Node::id));

    public void update(Node n) {
        byEnergyAscending.removeIf(existing -> existing.id().equals(n.id()));
        byEnergyAscending.add(n);
    }

    public SortedSet<Node> weakestFirst() {
        return byEnergyAscending;
    }
}
