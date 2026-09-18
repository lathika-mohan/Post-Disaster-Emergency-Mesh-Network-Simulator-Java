package com.meshsim.routing;

import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Minimum hop-count routing — ignores link quality entirely. */
public final class BFSRouter extends AbstractRouter {

    public BFSRouter(MeshNetwork net) {
        super(net);
    }

    @Override
    protected List<String> search(String sourceId, String destId) {
        Set<String> visited = new LinkedHashSet<>();
        Map<String, String> cameFrom = new LinkedHashMap<>();
        Deque<String> frontier = new ArrayDeque<>();
        frontier.add(sourceId);
        visited.add(sourceId);

        while (!frontier.isEmpty()) {
            String current = frontier.poll();
            if (current.equals(destId)) {
                return reconstruct(cameFrom, sourceId, destId);
            }
            for (Link link : network.neighborsOf(current)) {
                String neighborId = link.a().id().equals(current) ? link.b().id() : link.a().id();
                if (visited.add(neighborId)) {
                    cameFrom.put(neighborId, current);
                    frontier.add(neighborId);
                }
            }
        }
        return List.of();
    }

    private List<String> reconstruct(Map<String, String> cameFrom, String source, String dest) {
        List<String> path = new ArrayList<>();
        String step = dest;
        path.add(step);
        while (!step.equals(source)) {
            step = cameFrom.get(step);
            path.add(step);
        }
        java.util.Collections.reverse(path);
        return path;
    }

    @Override
    public String protocolName() {
        return "BFS";
    }
}
