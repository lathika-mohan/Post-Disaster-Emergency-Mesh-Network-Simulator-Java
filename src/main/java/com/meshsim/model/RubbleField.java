package com.meshsim.model;

public record RubbleField(Point centre, double radius, double density) implements Obstacle {
    @Override
    public boolean blocks(Point a, Point b) {
        return Obstacle.segmentIntersectsCircle(a, b, centre, radius);
    }

    @Override
    public double attenuationFactor() {
        return Math.max(0.1, 1.0 - density);
    }
}
