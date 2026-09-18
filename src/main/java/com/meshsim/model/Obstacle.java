package com.meshsim.model;

/**
 * A circular obstacle region in the disaster zone (flood, fire, rubble, collapse).
 * A link between two nodes is invalidated if the straight line between them
 * intersects the obstacle's circle.
 */
public class Obstacle {

    public enum Type { FLOOD_ZONE, FIRE_REGION, RUBBLE_FIELD, BUILDING_COLLAPSE }

    private final Type type;
    private final double x, y, radius;

    public Obstacle(Type type, double x, double y, double radius) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Type getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }

    /** Path-loss exponent multiplier associated with this obstacle's environment. */
    public double pathLossExponent() {
        switch (type) {
            case FLOOD_ZONE: return 3.5;
            case FIRE_REGION: return 4.0;
            case RUBBLE_FIELD: return 4.5;
            case BUILDING_COLLAPSE: return 5.5;
            default: return 2.0;
        }
    }

    /**
     * Returns true if the segment (x1,y1)-(x2,y2) intersects this circular obstacle,
     * using the standard point-to-segment distance test.
     */
    public boolean blocksSegment(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        double t = lenSq == 0 ? 0 : ((x - x1) * dx + (y - y1) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));
        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;
        double distSq = (closestX - x) * (closestX - x) + (closestY - y) * (closestY - y);
        return distSq <= radius * radius;
    }

    @Override
    public String toString() {
        return String.format("%s at (%.0f,%.0f) r=%.0f", type, x, y, radius);
    }
}
