package com.meshsim.network;

import com.meshsim.model.Node;

/** An undirected edge between two nodes in the mesh graph. */
public class Link {
    private final Node a, b;
    private final double distance;

    public Link(Node a, Node b, double distance) {
        this.a = a;
        this.b = b;
        this.distance = distance;
    }

    public Node getA() { return a; }
    public Node getB() { return b; }
    public double getDistance() { return distance; }

    public Node other(Node from) {
        return from.getId() == a.getId() ? b : a;
    }

    @Override
    public String toString() {
        return String.format("(%d <-> %d, d=%.1f)", a.getId(), b.getId(), distance);
    }
}
