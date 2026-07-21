package com.meshsim.routing;

import com.meshsim.model.Node;
import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import java.util.*;

/** Breadth-First Search router: O(V+E), returns the minimum hop-count path. */
public class BFSRouter implements Router {

    @Override
    public String name() { return "BFS (min hop-count)"; }

    @Override
    public Route findRoute(MeshNetwork network, int sourceId, int destId) {
        Node source = network.getNode(sourceId);
        Node dest = network.getNode(destId);
        if (source == null || dest == null || !source.isActive() || !dest.isActive()) {
            return Route.notFound();
        }

        Map<Integer, Integer> parent = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(sourceId);
        visited.add(sourceId);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == destId) {
                return Route.of(reconstruct(network, parent, sourceId, destId),
                        reconstruct(network, parent, sourceId, destId).size() - 1);
            }
            for (Link link : network.neighborsOf(current)) {
                Node next = link.other(network.getNode(current));
                if (!next.isActive() || visited.contains(next.getId())) continue;
                visited.add(next.getId());
                parent.put(next.getId(), current);
                queue.add(next.getId());
            }
        }
        return Route.notFound();
    }

    private List<Node> reconstruct(MeshNetwork network, Map<Integer, Integer> parent, int sourceId, int destId) {
        LinkedList<Node> path = new LinkedList<>();
        Integer cur = destId;
        while (cur != null) {
            path.addFirst(network.getNode(cur));
            if (cur == sourceId) break;
            cur = parent.get(cur);
        }
        return path;
    }
}
