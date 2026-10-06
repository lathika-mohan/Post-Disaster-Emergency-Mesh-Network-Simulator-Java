package com.meshsim.network;

import com.meshsim.model.Node;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;

import java.util.List;

/**
 * Greedy relay placement advisor: finds optimal coordinate to reconnect isolated survivors.
 */
public final class RelayPlacementAdvisor {

    private RelayPlacementAdvisor() {
    }

    public static Point suggestRelayPosition(Scenario scenario, MeshNetwork network) {
        NetworkPartitionDetector.PartitionResult partitions = NetworkPartitionDetector.detectPartitions(network);
        if (partitions.islandCount() <= 1 || partitions.isolatedSurvivors().isEmpty()) {
            return null; // Network is fully connected or no survivors stranded
        }

        // Find centroid of isolated survivors and nearest main-component node
        double sumX = 0, sumY = 0;
        int count = 0;
        for (String id : partitions.isolatedSurvivors()) {
            scenario.nodes().find(id).ifPresent(n -> {
                // accumulator
            });
            Node n = scenario.nodes().find(id).orElse(null);
            if (n != null) {
                sumX += n.position().x();
                sumY += n.position().y();
                count++;
            }
        }
        if (count == 0) return null;

        Point centroid = new Point(sumX / count, sumY / count);

        // Find closest main component node
        List<Node> mainNodes = partitions.components().isEmpty() ? List.of() :
                partitions.components().get(0).stream()
                        .map(id -> scenario.nodes().find(id).orElse(null))
                        .filter(n -> n != null)
                        .toList();

        Point bestMainPoint = null;
        double minDst = Double.MAX_VALUE;
        for (Node mn : mainNodes) {
            double d = mn.position().distanceTo(centroid);
            if (d < minDst) {
                minDst = d;
                bestMainPoint = mn.position();
            }
        }

        if (bestMainPoint == null) return centroid;

        // Suggested position is midpoint between isolated centroid and main component
        return new Point((centroid.x() + bestMainPoint.x()) / 2.0, (centroid.y() + bestMainPoint.y()) / 2.0);
    }
}
