package com.meshsim.model;

import com.meshsim.util.Identifiable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A mesh node. Deliberately exercises all four access specifiers (Unit 1):
 *  - private fields: internal mutable state, nobody outside touches it directly
 *  - package-private helper (applyMovementDelta): only com.meshsim.model/network
 *    code should nudge position directly; everyone else goes through moveTo
 *  - protected method (onEnergyDepleted): subclasses (if any) may override
 *    what "running out of battery" means
 *  - public API: the contract the rest of the application relies on
 */
public class Node implements Identifiable {

    // Unit 1 "static members": a private static counter behind a public static factory.
    private static final AtomicInteger nextId = new AtomicInteger(1);

    private final String id;
    private final NodeType type;
    private Point position;
    private double energy;      // 0.0 - 1.0
    private boolean alive = true;

    private Node(String id, NodeType type, Point position, double energy) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.energy = energy;
    }

    /** Static factory (Unit 1): the only supported way to create a Node with an auto id. */
    public static Node spawn(NodeType type, Point position) {
        return new Node("N" + nextId.getAndIncrement(), type, position, 1.0);
    }

    /** Factory used by the CSV loader / DAO layer, where the id is already known. */
    public static Node withId(String id, NodeType type, Point position, double energy) {
        return new Node(id, type, position, energy);
    }

    @Override
    public String id() {
        return id;
    }

    public NodeType type() {
        return type;
    }

    public Point position() {
        return position;
    }

    public double energy() {
        return energy;
    }

    public boolean isAlive() {
        return alive;
    }

    public NodeSnapshot snapshot() {
        return new NodeSnapshot(id, type, position, energy, alive);
    }

    public void moveTo(Point newPosition) {
        if (!type.isMobile()) return;
        this.position = newPosition;
    }

    /** Package-private: only network-package movement code may nudge position directly. */
    void applyMovementDelta(double dx, double dy) {
        this.position = new Point(position.x() + dx, position.y() + dy);
    }

    public void drain(double amount) {
        energy = Math.max(0.0, energy - amount);
        if (energy <= 0.0 && alive) {
            alive = false;
            onEnergyDepleted();
        }
    }

    public void recharge(double amount) {
        energy = Math.min(1.0, energy + amount);
    }

    /** Protected hook: subclasses may customize what happens when a node dies. */
    protected void onEnergyDepleted() {
        // default: no-op; SimulationLogger listens via the event bus instead
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "%s[%s @ %s, energy=%.2f, alive=%b]".formatted(id, type, position, energy, alive);
    }
}
