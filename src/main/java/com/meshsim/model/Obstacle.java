package com.meshsim.model;

/**
 * Sealed hierarchy of disaster obstacles. Sealing forces every switch over
 * Obstacle to handle every permitted subtype at compile time (Unit 2 / Unit 5
 * "Features of Java 21": sealed classes, pattern matching for switch).
 */
public sealed interface Obstacle
        permits FloodZone, FireRegion, RubbleField, CollapsedBuilding {

    Point centre();

    /** Does the straight line a-b pass through this obstacle? */
    boolean blocks(Point a, Point b);

    /** Multiplicative signal attenuation factor in [0, 1]; 0 = fully blocks. */
    double attenuationFactor();

    /**
     * Human-readable description using pattern matching for switch with
     * record patterns (Unit 2 / Unit 5, Java 21).
     */
    static String describe(Obstacle o) {
        return switch (o) {
            case FloodZone(Point c, double r, double depth) when depth > 2.0 ->
                    "Deep flood (depth %.1fm) radius %.1f at %s".formatted(depth, r, c);
            case FloodZone f ->
                    "Shallow flood, radius %.1f at %s".formatted(f.radius(), f.centre());
            case FireRegion f ->
                    "Fire front, spread rate %.2f at %s".formatted(f.spreadRate(), f.centre());
            case RubbleField r ->
                    "Rubble field, density %.2f at %s".formatted(r.density(), r.centre());
            case CollapsedBuilding b ->
                    "Collapsed building, %d floors at %s".formatted(b.floors(), b.centre());
            // no default needed - the interface is sealed and every case is covered
        };
    }

    /** Simple circle-based line-of-sight blocking test shared by circular obstacles. */
    static boolean segmentIntersectsCircle(Point a, Point b, Point centre, double radius) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0) {
            return a.distanceTo(centre) <= radius;
        }
        double t = ((centre.x() - a.x()) * dx + (centre.y() - a.y()) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));
        Point closest = new Point(a.x() + t * dx, a.y() + t * dy);
        return closest.distanceTo(centre) <= radius;
    }
}
