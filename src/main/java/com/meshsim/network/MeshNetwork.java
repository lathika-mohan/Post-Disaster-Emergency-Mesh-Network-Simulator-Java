package com.meshsim.network;

import com.meshsim.model.Environment;
import com.meshsim.model.Node;

import java.util.*;

/**
 * Builds and maintains the mesh graph G = (V, E).
 * Two nodes are connected when d(A,B) <= min(rangeA, rangeB) AND no obstacle
 * blocks the straight line between them (obstacle-aware link validity).
 */
public class MeshNetwork {
    private final Environment environment;
    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Map<Integer, List<Link>> adjacency = new HashMap<>();

    public MeshNetwork(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() { return environment; }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacency.put(node.getId(), new ArrayList<>());
        rebuildLinksFor(node);
    }

    public void removeNode(int id) {
        nodes.remove(id);
        adjacency.remove(id);
        for (List<Link> links : adjacency.values()) {
            links.removeIf(l -> l.getA().getId() == id || l.getB().getId() == id);
        }
    }

    public Collection<Node> getNodes() { return nodes.values(); }
    public Node getNode(int id) { return nodes.get(id); }

    public static double distance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Recomputes the full adjacency graph from scratch (call after node failures / moves). */
    public void rebuildAllLinks() {
        for (List<Link> l : adjacency.values()) l.clear();
        List<Node> active = new ArrayList<>();
        for (Node n : nodes.values()) if (n.isActive()) active.add(n);
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                tryLink(active.get(i), active.get(j));
            }
        }
    }

    private void rebuildLinksFor(Node node) {
        if (!node.isActive()) return;
        for (Node other : nodes.values()) {
            if (other.getId() == node.getId() || !other.isActive()) continue;
            tryLink(node, other);
        }
    }

    private void tryLink(Node a, Node b) {
        double d = distance(a, b);
        boolean inRange = d <= Math.min(a.getRange(), b.getRange());
        boolean blocked = environment.isLinkBlocked(a.getX(), a.getY(), b.getX(), b.getY());
        if (inRange && !blocked) {
            Link link = new Link(a, b, d);
            adjacency.get(a.getId()).add(link);
            adjacency.get(b.getId()).add(new Link(b, a, d));
        }
    }

    public List<Link> neighborsOf(int nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    /** Total number of undirected edges currently in the graph. */
    public int edgeCount() {
        int total = 0;
        for (List<Link> l : adjacency.values()) total += l.size();
        return total / 2;
    }

    /** Number of nodes still transmitting (energy > 0 and not manually downed). */
    public long activeNodeCount() {
        return nodes.values().stream().filter(Node::isActive).count();
    }

    public int totalNodeCount() { return nodes.size(); }

    /** Number of connected components among currently active nodes (1 = fully connected mesh). */
    public int connectedComponents() {
        Set<Integer> visited = new HashSet<>();
        int components = 0;
        for (Node n : nodes.values()) {
            if (!n.isActive() || visited.contains(n.getId())) continue;
            components++;
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(n.getId());
            visited.add(n.getId());
            while (!stack.isEmpty()) {
                int current = stack.pop();
                for (Link link : neighborsOf(current)) {
                    Node next = link.other(getNode(current));
                    if (next.isActive() && visited.add(next.getId())) {
                        stack.push(next.getId());
                    }
                }
            }
        }
        return components;
    }

    /** Average node degree = 2E / V, over active nodes. */
    public double averageDegree() {
        long v = activeNodeCount();
        return v == 0 ? 0 : (2.0 * edgeCount()) / v;
    }

    /** Graph density = 2E / (V * (V-1)), over active nodes. */
    public double density() {
        long v = activeNodeCount();
        return v <= 1 ? 0 : (2.0 * edgeCount()) / (v * (v - 1));
    }
}
