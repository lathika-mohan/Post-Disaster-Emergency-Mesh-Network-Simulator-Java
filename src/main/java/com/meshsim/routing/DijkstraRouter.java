package com.meshsim.routing;

import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;

/** Weighted shortest path where weight is derived from link quality, not raw distance. */
public class DijkstraRouter extends AbstractRouter {

    private record Candidate(String nodeId, double cost) {
    }

    public DijkstraRouter(MeshNetwork net) {
        super(net);
    }

    @Override
    protected List<String> search(String sourceId, String destId) {
        Map<String, Double> best = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> settled = new HashSet<>();
        PriorityQueue<Candidate> frontier =
                new PriorityQueue<>(Comparator.comparingDouble(Candidate::cost));

        best.put(sourceId, 0.0);
        frontier.add(new Candidate(sourceId, 0.0));

        while (!frontier.isEmpty()) {
            Candidate current = frontier.poll();
            if (!settled.add(current.nodeId())) continue;
            if (current.nodeId().equals(destId)) break;

            for (Link link : network.neighborsOf(current.nodeId())) {
                String neighborId = link.a().id().equals(current.nodeId())
                        ? link.b().id() : link.a().id();
                double edgeWeight = linkWeight(link);
                double candidateCost = current.cost() + edgeWeight;
                if (candidateCost < best.getOrDefault(neighborId, Double.MAX_VALUE)) {
                    best.put(neighborId, candidateCost);
                    cameFrom.put(neighborId, current.nodeId());
                    frontier.add(new Candidate(neighborId, candidateCost));
                }
            }
        }

        if (!cameFrom.containsKey(destId) && !sourceId.equals(destId)) {
            return List.of();
        }
        return reconstruct(cameFrom, sourceId, destId);
    }

    /** Hook so EnergyAwareDijkstraRouter can change only the weight function. */
    protected double linkWeight(Link link) {
        return link.weight();
    }

    private List<String> reconstruct(Map<String, String> cameFrom, String source, String dest) {
        List<String> path = new ArrayList<>();
        String step = dest;
        path.add(step);
        while (!step.equals(source)) {
            step = cameFrom.get(step);
            if (step == null) return List.of();
            path.add(step);
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public String protocolName() {
        return "Dijkstra";
    }
}
