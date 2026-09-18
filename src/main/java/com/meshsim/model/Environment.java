package com.meshsim.model;

import java.util.ArrayList;
import java.util.List;

/** The 2D disaster-zone plane containing obstacles (Environment Layer). */
public class Environment {
    private final double width;
    private final double height;
    private final List<Obstacle> obstacles = new ArrayList<>();

    public Environment(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public void addObstacle(Obstacle o) { obstacles.add(o); }

    /** True if any obstacle blocks the straight line between the two points. */
    public boolean isLinkBlocked(double x1, double y1, double x2, double y2) {
        for (Obstacle o : obstacles) {
            if (o.blocksSegment(x1, y1, x2, y2)) return true;
        }
        return false;
    }

    public boolean isInBounds(double x, double y) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }
}
