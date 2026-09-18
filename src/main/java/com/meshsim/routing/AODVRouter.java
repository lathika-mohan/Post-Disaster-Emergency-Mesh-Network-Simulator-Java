package com.meshsim.routing;

import com.meshsim.network.Link;
import com.meshsim.network.MeshNetwork;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified reactive AODV: flood a route request outward, the first RREQ to
 * reach the destination wins, RREP walks the reverse path back, and the
 * discovered route is cached with a short lifetime so repeat sends don't
 * re-flood the network immediately.
 */
public final class AODVRouter extends AbstractRouter {

    private record CachedRoute(List<String> hops, long expiresAtNanos) {
    }

    private static final long ROUTE_LIFETIME_NANOS = 2_000_000_000L; // 2 seconds of sim wall time
    private final Map<String, CachedRoute> routeCache = new ConcurrentHashMap<>();

    public AODVRouter(MeshNetwork net) {
        super(net);
    }

    @Override
    protected List<String> search(String sourceId, String destId) {
        String key = sourceId + "->" + destId;
        CachedRoute cached = routeCache.get(key);
        if (cached != null && cached.expiresAtNanos() > System.nanoTime()) {
            return cached.hops();
        }

        List<String> discovered = floodRreq(sourceId, destId);
        if (!discovered.isEmpty()) {
            routeCache.put(key, new CachedRoute(discovered, System.nanoTime() + ROUTE_LIFETIME_NANOS));
        }
        return discovered;
    }

    private List<String> floodRreq(String sourceId, String destId) {
        Set<String> visited = new HashSet<>();
        Map<String, String> reverseHop = new HashMap<>();
        Deque<String> frontier = new ArrayDeque<>();
        frontier.add(sourceId);
        visited.add(sourceId);

        while (!frontier.isEmpty()) {
            String current = frontier.poll();
            if (current.equals(destId)) {
                // RREP walks the reverse path back to the source
                List<String> path = new ArrayList<>();
                String step = destId;
                path.add(step);
                while (!step.equals(sourceId)) {
                    step = reverseHop.get(step);
                    path.add(step);
                }
                java.util.Collections.reverse(path);
                return path;
            }
            for (Link link : network.neighborsOf(current)) {
                String neighborId = link.a().id().equals(current) ? link.b().id() : link.a().id();
                if (visited.add(neighborId)) {
                    reverseHop.put(neighborId, current);
                    frontier.add(neighborId);
                }
            }
        }
        return List.of();
    }

    @Override
    public String protocolName() {
        return "AODV";
    }
}
