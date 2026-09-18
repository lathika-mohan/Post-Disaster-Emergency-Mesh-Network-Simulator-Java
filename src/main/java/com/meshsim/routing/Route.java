package com.meshsim.routing;

import com.meshsim.model.Node;

import java.util.Collections;
import java.util.List;

/** Result of a route-finding attempt between two nodes. */
public class Route {
    private final boolean found;
    private final List<Node> path;
    private final double totalCost; // meaning depends on router (hop count or weighted distance)

    private Route(boolean found, List<Node> path, double totalCost) {
        this.found = found;
        this.path = path;
        this.totalCost = totalCost;
    }

    public static Route notFound() {
        return new Route(false, Collections.emptyList(), Double.POSITIVE_INFINITY);
    }

    public static Route of(List<Node> path, double totalCost) {
        return new Route(true, path, totalCost);
    }

    public boolean isFound() { return found; }
    public List<Node> getPath() { return path; }
    public int getHopCount() { return path.isEmpty() ? 0 : path.size() - 1; }
    public double getTotalCost() { return totalCost; }

    public String pathString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getId());
            if (i < path.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}
