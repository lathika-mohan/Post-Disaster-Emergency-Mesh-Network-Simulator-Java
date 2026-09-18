package com.meshsim.model;

public record FloodZone(Point centre, double radius, double depthMetres) implements Obstacle {
    @Override
    public boolean blocks(Point a, Point b) {
        return Obstacle.segmentIntersectsCircle(a, b, centre, radius);
    }

    @Override
    public double attenuationFactor() {
        return depthMetres > 2.0 ? 0.05 : 0.35;
    }
}
