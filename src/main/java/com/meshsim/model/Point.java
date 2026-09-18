package com.meshsim.model;

/** Immutable 2D coordinate. Java 21 record (Unit 2 / Unit 5 "Features of Java 21"). */
public record Point(double x, double y) {

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "(%.1f, %.1f)".formatted(x, y);
    }
}
