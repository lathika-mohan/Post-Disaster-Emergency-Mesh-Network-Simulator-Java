package com.meshsim.model;

public record CollapsedBuilding(Point centre, double radius, int floors) implements Obstacle {
    @Override
    public boolean blocks(Point a, Point b) {
        return Obstacle.segmentIntersectsCircle(a, b, centre, radius);
    }

    @Override
    public double attenuationFactor() {
        return Math.max(0.02, 1.0 - floors * 0.15);
    }
}
