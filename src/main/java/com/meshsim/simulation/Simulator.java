package com.meshsim.simulation;

import com.meshsim.energy.EnergyModel;
import com.meshsim.model.Node;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.Route;
import com.meshsim.routing.Router;

import java.util.List;

/** Drives the simulation: message sending, energy depletion, node-failure handling. */
public class Simulator {
    private final MeshNetwork network;
    private final EnergyModel energyModel = new EnergyModel();
    private final Metrics metrics = new Metrics();
    private int tick = 0;
    private static final double DEFAULT_PATH_LOSS_EXPONENT = 2.0; // free space

    public Simulator(MeshNetwork network) {
        this.network = network;
    }

    public MeshNetwork getNetwork() { return network; }
    public Metrics getMetrics() { return metrics; }
    public int getTick() { return tick; }

    /**
     * Attempts to deliver an emergency message from source to destination using
     * the supplied routing protocol. Applies energy costs hop-by-hop and
     * automatically deactivates nodes whose energy reaches zero.
     */
    public Route sendMessage(int sourceId, int destId, Router router) {
        tick++;
        metrics.recordAttempt();
        Route route = router.findRoute(network, sourceId, destId);

        if (!route.isFound()) {
            return route;
        }

        List<Node> path = route.getPath();
        boolean anyFailure = false;
        for (int i = 0; i < path.size() - 1; i++) {
            Node sender = path.get(i);
            Node receiver = path.get(i + 1);
            double dist = MeshNetwork.distance(sender, receiver);

            double txEnergy = energyModel.transmitEnergy(dist, DEFAULT_PATH_LOSS_EXPONENT);
            double rxEnergy = energyModel.receiveEnergy();

            sender.consumeEnergy(txEnergy);
            receiver.consumeEnergy(rxEnergy);
            metrics.recordEnergyUse(txEnergy + rxEnergy);

            if (!sender.isActive() || !receiver.isActive()) {
                anyFailure = true;
                metrics.recordFirstFailure(tick);
            }
        }

        if (anyFailure) {
            long start = System.currentTimeMillis();
            network.rebuildAllLinks();
            long recoveryMs = System.currentTimeMillis() - start;
            metrics.recordRecoveryTime(recoveryMs);
        }

        metrics.recordDelivery(route.getHopCount());
        return route;
    }

    /**
     * Manually simulates a node going down (e.g. device destroyed / battery dead).
     * Returns the graph-rebuild time in milliseconds, or -1 if the node id was invalid.
     */
    public long failNode(int nodeId) {
        Node n = network.getNode(nodeId);
        if (n == null) return -1;
        n.deactivate();
        metrics.recordFirstFailure(tick);
        long start = System.currentTimeMillis();
        network.rebuildAllLinks();
        long recoveryMs = System.currentTimeMillis() - start;
        metrics.recordRecoveryTime(recoveryMs);
        return recoveryMs;
    }

    /** Coverage ratio: reachable survivors / total survivors, from a given rescue node. */
    public double coverageRatio(int rescueId, Router router) {
        List<Node> survivors = network.getNodes().stream()
                .filter(n -> n.getType() == com.meshsim.model.NodeType.SURVIVOR)
                .toList();
        if (survivors.isEmpty()) return 0;
        long reachable = survivors.stream()
                .filter(s -> router.findRoute(network, rescueId, s.getId()).isFound())
                .count();
        return (double) reachable / survivors.size() * 100.0;
    }

    /**
     * Overall coverage ratio: fraction of active survivors that have a route
     * to AT LEAST ONE active rescue node (does not require picking a rescue id).
     */
    public double overallCoverageRatio(Router router) {
        List<Node> survivors = network.getNodes().stream()
                .filter(n -> n.getType() == com.meshsim.model.NodeType.SURVIVOR && n.isActive())
                .toList();
        List<Node> rescuers = network.getNodes().stream()
                .filter(n -> n.getType() == com.meshsim.model.NodeType.RESCUE && n.isActive())
                .toList();
        if (survivors.isEmpty() || rescuers.isEmpty()) return 0;
        long reachable = survivors.stream()
                .filter(s -> rescuers.stream().anyMatch(r -> router.findRoute(network, s.getId(), r.getId()).isFound()))
                .count();
        return (double) reachable / survivors.size() * 100.0;
    }

    public long survivorCount() {
        return network.getNodes().stream().filter(n -> n.getType() == com.meshsim.model.NodeType.SURVIVOR).count();
    }

    public long activeSurvivorCount() {
        return network.getNodes().stream()
                .filter(n -> n.getType() == com.meshsim.model.NodeType.SURVIVOR && n.isActive()).count();
    }

    /** Percentage of all deployed nodes still alive (a proxy for "network lifetime remaining"). */
    public double networkLifetimePercent() {
        int total = network.totalNodeCount();
        return total == 0 ? 0 : (double) network.activeNodeCount() / total * 100.0;
    }
}
