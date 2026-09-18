package com.meshsim.model;

import com.meshsim.util.Identifiable;
import com.meshsim.util.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * A full, cloneable simulation setup: nodes plus obstacles on a bounded field.
 * Cloneable + a deep clone() (Unit 2) so experiments can start from identical state.
 */
public class Scenario implements Identifiable, Cloneable {

    private final String id;
    private String name;
    private final double width;
    private final double height;
    private final Repository<Node> nodes = new Repository<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    public Scenario(String id, String name, double width, double height) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
    }

    @Override
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public Repository<Node> nodes() {
        return nodes;
    }

    public List<Obstacle> obstacles() {
        return obstacles;
    }

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addObstacle(Obstacle o) {
        obstacles.add(o);
    }

    /** Deep clone: a fresh Scenario with copied node state (obstacles are immutable records). */
    @Override
    public Scenario clone() {
        Scenario copy = new Scenario(id, name, width, height);
        for (Node n : nodes) {
            copy.addNode(Node.withId(n.id(), n.type(), n.position(), n.energy()));
        }
        copy.obstacles.addAll(this.obstacles); // records are immutable - safe to share
        return copy;
    }

    @Override
    public String toString() {
        return "Scenario[%s, %d nodes, %d obstacles, %.0fx%.0f]"
                .formatted(name, nodes.size(), obstacles.size(), width, height);
    }
}
