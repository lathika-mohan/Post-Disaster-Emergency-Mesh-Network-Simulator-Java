package com.meshsim.network;

import com.meshsim.exception.NetworkPartitionedException;
import com.meshsim.model.Node;
import com.meshsim.model.Scenario;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The live topology graph, rebuilt whenever nodes move. Readers (routers) and
 * the one writer (mobility engine, Phase 6) contend for this, so it is guarded
 * with a ReentrantReadWriteLock rather than a single coarse `synchronized` —
 * many routers can read concurrently, only the rebuild needs exclusivity.
 */
public class MeshNetwork {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, List<Link>> adjacency = new HashMap<>();
    private Scenario scenario;

    public MeshNetwork(Scenario scenario) {
        this.scenario = scenario;
        rebuild();
    }

    public Scenario scenario() {
        return scenario;
    }

    /** Recomputes every pairwise link from current node positions and obstacles. */
    public void rebuild() {
        lock.writeLock().lock();
        try {
            adjacency.clear();
            List<Node> nodeList = new ArrayList<>();
            for (Node n : scenario.nodes()) {
                if (n.isAlive()) nodeList.add(n);
                adjacency.put(n.id(), new ArrayList<>());
            }
            for (int i = 0; i < nodeList.size(); i++) {
                for (int j = i + 1; j < nodeList.size(); j++) {
                    Node a = nodeList.get(i);
                    Node b = nodeList.get(j);
                    double quality = PathLossModel.computeQuality(a, b, scenario);
                    if (quality > 0.0) {
                        Link link = new Link(a, b, quality);
                        adjacency.get(a.id()).add(link);
                        adjacency.get(b.id()).add(link);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Link> neighborsOf(String nodeId) {
        lock.readLock().lock();
        try {
            return List.copyOf(adjacency.getOrDefault(nodeId, List.of()));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * BFS connected-components check between src and dst. Throws
     * NetworkPartitionedException with both component sizes if they are
     * not reachable from one another at all.
     */
    public void assertConnected(String srcId, String dstId) throws NetworkPartitionedException {
        lock.readLock().lock();
        try {
            Set<String> componentOfSrc = reachableFrom(srcId);
            if (componentOfSrc.contains(dstId)) {
                return;
            }
            Set<String> componentOfDst = reachableFrom(dstId);
            throw new NetworkPartitionedException(componentOfSrc.size(), componentOfDst.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    private Set<String> reachableFrom(String start) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> frontier = new ArrayDeque<>();
        frontier.add(start);
        visited.add(start);
        while (!frontier.isEmpty()) {
            String current = frontier.poll();
            for (Link link : adjacency.getOrDefault(current, List.of())) {
                String neighborId = link.other(findNode(current)).id();
                if (visited.add(neighborId)) {
                    frontier.add(neighborId);
                }
            }
        }
        return visited;
    }

    private Node findNode(String id) {
        return scenario.nodes().find(id)
                .orElseThrow(() -> new IllegalStateException("Unknown node id in graph: " + id));
    }
}
