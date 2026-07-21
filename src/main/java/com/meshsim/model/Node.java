package com.meshsim.model;

/** A single device in the mesh network (survivor, rescuer, or relay). */
public class Node {
    private final int id;
    private double x, y;
    private final NodeType type;
    private double range;          // transmission radius in the 2D plane (px)
    private double energy;         // residual energy in Joules
    private final double maxEnergy;
    private boolean active = true; // false once energy is depleted or node manually removed

    public Node(int id, double x, double y, NodeType type, double range, double energy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.range = range;
        this.energy = energy;
        this.maxEnergy = energy;
    }

    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public NodeType getType() { return type; }
    public double getRange() { return range; }
    public double getEnergy() { return energy; }
    public double getMaxEnergy() { return maxEnergy; }
    public boolean isActive() { return active && energy > 0; }
    public void deactivate() { active = false; }
    public void activate() { active = true; }

    /** Consume energy; deactivates the node automatically when depleted. */
    public void consumeEnergy(double joules) {
        energy = Math.max(0, energy - joules);
        if (energy <= 0) active = false;
    }

    public double energyPercent() {
        return maxEnergy == 0 ? 0 : (energy / maxEnergy) * 100.0;
    }

    @Override
    public String toString() {
        return String.format("Node#%d[%s] pos=(%.0f,%.0f) range=%.0f energy=%.1f%% %s",
                id, type, x, y, range, energyPercent(), isActive() ? "ACTIVE" : "DOWN");
    }
}
