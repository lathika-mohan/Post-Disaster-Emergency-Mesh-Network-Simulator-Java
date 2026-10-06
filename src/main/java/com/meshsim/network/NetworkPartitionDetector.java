package com.meshsim.network;

import com.meshsim.model.Node;

import java.util.*;

/**
. * Connected-components analysis over MeshNetwork.
 */
public final class NetworkPartitionDetector {

    private NetworkPartitionDetector() {
    }

    public static record PartitionResult(int islandCount, List<Set<String>> components, List<String> isolatedSurvivors) {
    }

    public static PartitionResult detectPartitions(MeshNetwork network) {
        Set<String> visited = new HashSet<>();
        List<Set<String>> components = new ArrayList<>();
        List<String> isolatedSurvivors = new ArrayList<>();

        for (Node node : network.scenario().nodes()) {
            if (!node.isAlive()) continue;
            String id = node.id();
            if (visited.contains(id)) continue;

            Set<String> component = new HashSet<>();
            Queue<String> queue = new LinkedList<>();
            queue.add(id);
            visited.add(id);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                component.add(current);

                for (Link link : network.neighborsOf(current)) {
                    Node neighborNode = link.b();
                    String neighborId = neighborNode.id();
                    if (neighborNode.isAlive() && !visited.contains(neighborId)) {
                        visited.add(neighborId);
                        queue.add(neighborId);
                    }
                }
            }
            components.add(component);
        }

        if (!components.isEmpty()) {
            Set<String> mainComponent = components.stream().max(Comparator.comparingInt(Set::size)).orElse(Collections.emptySet());
            for (Node n : network.scenario().nodes()) {
                if (n.type() == com.meshsim.model.NodeType.SURVIVOR && n.isAlive() && !mainComponent.contains(n.id())) {
                    isolatedSurvivors.add(n.id());
                }
            }
        }

        return new PartitionResult(components.size(), components, isolatedSurvivors);
    }
}
