package com.meshsim.network;

import com.meshsim.model.Node;

import java.util.Objects;

/** An edge between two nodes with a computed quality (0..1) used as routing weight input. */
public final class Link {

    private final Node a;
    private final Node b;
    private final double quality;

    public Link(Node a, Node b, double quality) {
        this.a = a;
        this.b = b;
        this.quality = quality;
    }

    public Node a() {
        return a;
    }

    public Node b() {
        return b;
    }

    public double quality() {
        return quality;
    }

    public Node other(Node n) {
        if (n.equals(a)) return b;
        if (n.equals(b)) return a;
        throw new IllegalArgumentException("Node " + n.id() + " is not part of this link");
    }

    /** Routing weight: cheaper (lower) for higher-quality links. */
    public double weight() {
        return 1.0 / Math.max(0.01, quality);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link other)) return false;
        return (a.equals(other.a) && b.equals(other.b))
                || (a.equals(other.b) && b.equals(other.a));
    }

    @Override
    public int hashCode() {
        // order-independent so a-b and b-a hash the same
        return Objects.hash(a.id()) ^ Objects.hash(b.id());
    }

    @Override
    public String toString() {
        return "%s<->%s (q=%.2f)".formatted(a.id(), b.id(), quality);
    }
}
