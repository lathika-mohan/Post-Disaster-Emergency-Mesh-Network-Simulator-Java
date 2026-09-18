package com.meshsim.model;

public record FireRegion(Point centre, double radius, double spreadRate) implements Obstacle {
    @Override
    public boolean blocks(Point a, Point b) {
        return Obstacle.segmentIntersectsCircle(a, b, centre, radius);
    }

    @Override
    public double attenuationFactor() {
        return 0.0; // fire fronts fully block line of sight
    }

    /** Returns a new FireRegion grown by one tick's spread. */
    public FireRegion grow() {
        return new FireRegion(centre, radius + spreadRate, spreadRate);
    }
}
