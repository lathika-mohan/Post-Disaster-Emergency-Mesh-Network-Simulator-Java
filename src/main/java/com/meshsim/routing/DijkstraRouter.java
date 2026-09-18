package com.meshsim.routing;

import com.meshsim.model.Node;
import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import java.util.*;

/**
 * Dijkstra router: O(E + V log V), minimizes cumulative Euclidean distance
 * (a proxy for transmission energy / latency along the route).
 */
public class DijkstraRouter implements Router {

    @Override
    public String name() { return "Dijkstra (min weighted cost)"; }

    @Override
    public Route findRoute(MeshNetwork network, int sourceId, int destId) {
        Node source = network.getNode(sourceId);
        Node dest = network.getNode(destId);
        if (source == null || dest == null || !source.isActive() || !dest.isActive()) {
            return Route.notFound();
        }

        Map<Integer, Double> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        for (Node n : network.getNodes()) dist.put(n.getId(), Double.POSITIVE_INFINITY);
        dist.put(sourceId, 0.0);

        // frontier entries: {nodeId, cumulativeDistance}
        PriorityQueue<double[]> frontier = new PriorityQueue<>(Comparator.comparingDouble(e -> e[1]));
        frontier.add(new double[]{sourceId, 0.0});
        Set<Integer> settled = new HashSet<>();

        while (!frontier.isEmpty()) {
            double[] top = frontier.poll();
            int current = (int) top[0];
            if (settled.contains(current)) continue;
            settled.add(current);
            if (current == destId) break;

            for (Link link : network.neighborsOf(current)) {
                Node next = link.other(network.getNode(current));
                if (!next.isActive()) continue;
                double newDist = dist.get(current) + link.getDistance();
                if (newDist < dist.getOrDefault(next.getId(), Double.POSITIVE_INFINITY)) {
                    dist.put(next.getId(), newDist);
                    parent.put(next.getId(), current);
                    frontier.add(new double[]{next.getId(), newDist});
                }
            }
        }

        if (dist.getOrDefault(destId, Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY) {
            return Route.notFound();
        }

        LinkedList<Node> path = new LinkedList<>();
        Integer cur = destId;
        while (cur != null) {
            path.addFirst(network.getNode(cur));
            if (cur == sourceId) break;
            cur = parent.get(cur);
        }
        return Route.of(path, dist.get(destId));
    }
}
