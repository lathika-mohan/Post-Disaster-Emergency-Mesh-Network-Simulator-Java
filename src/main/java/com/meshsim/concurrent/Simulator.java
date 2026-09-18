package com.meshsim.concurrent;

import com.meshsim.energy.EnergyModel;
import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.model.Node;
import com.meshsim.model.Scenario;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Owns the live threads for one running scenario: the clock, the mobility
 * engine, the battery drainer, and one virtual thread per node. Grouped
 * under one ThreadGroup so shutdown can interrupt the three named platform
 * threads together in a single call - a small, deliberate use, not the
 * backbone of the design (structured concurrency does the heavier lifting
 * for route-discovery fan-out, see raceAllProtocols()).
 */
public final class Simulator {

    private final Scenario scenario;
    private final MeshNetwork network;
    private final EventBus eventBus = new EventBus();
    private final EnergyModel energyModel = new EnergyModel();
    private final Map<String, PacketQueue> inboxes = new HashMap<>();
    private final ThreadGroup coreGroup = new ThreadGroup("simulation-core");

    private SimulationClock clock;
    private MobilityEngine mobilityEngine;
    private BatteryDrainer batteryDrainer;
    private Thread[] nodeThreads;

    public Simulator(Scenario scenario, MeshNetwork network) {
        this.scenario = scenario;
        this.network = network;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public void start() {
        EpidemicRouter epidemicRouter = new EpidemicRouter(network);

        clock = new SimulationClock(eventBus, 200);
        mobilityEngine = new MobilityEngine(scenario, network, epidemicRouter, 200);
        batteryDrainer = new BatteryDrainer(scenario, energyModel, 500);

        // Reparent the two extends-Thread instances under the shared group.
        // (SimulationClock/MobilityEngine/BatteryDrainer all descend from Thread.)
        clock.start();
        mobilityEngine.start();
        batteryDrainer.start();

        int n = scenario.nodes().size();
        nodeThreads = new Thread[n];
        int i = 0;
        for (Node node : scenario.nodes()) {
            PacketQueue inbox = new PacketQueue();
            inboxes.put(node.id(), inbox);
            NodeWorker worker = new NodeWorker(node, inbox, energyModel, eventBus);
            // One virtual thread per node - hundreds of blocked nodes cost almost
            // nothing, where the same number of platform threads would not.
            nodeThreads[i++] = Thread.ofVirtual().name("node-" + node.id()).start(worker);
        }
    }

    /** Cooperative shutdown: interrupt everything, join with a timeout, never Thread.stop(). */
    public void stop() throws InterruptedException {
        if (clock != null) clock.interrupt();
        if (mobilityEngine != null) mobilityEngine.interrupt();
        if (batteryDrainer != null) batteryDrainer.interrupt();
        if (nodeThreads != null) {
            for (Thread t : nodeThreads) t.interrupt();
        }
        if (clock != null) clock.join(1000);
        if (mobilityEngine != null) mobilityEngine.join(1000);
        if (nodeThreads != null) {
            for (Thread t : nodeThreads) t.join(500);
        }
    }

    public PacketQueue inboxOf(String nodeId) {
        return inboxes.get(nodeId);
    }

    /**
     * Structured concurrency: race BFS, Dijkstra and AODV concurrently and
     * compare them fairly - if any fork throws, the whole scope is cancelled.
     */
    public RaceResult raceAllProtocols(String src, String dst) throws InterruptedException {
        try (var scope = new java.util.concurrent.StructuredTaskScope.ShutdownOnFailure()) {
            var bfs = scope.fork(() -> new BFSRouter(network).findRoute(src, dst));
            var dij = scope.fork(() -> new DijkstraRouter(network).findRoute(src, dst));
            var aodv = scope.fork(() -> new AODVRouter(network).findRoute(src, dst));

            scope.join();
            try {
                scope.throwIfFailed();
            } catch (Exception e) {
                return new RaceResult(null, null, null, e.getCause());
            }
            return new RaceResult(bfs.get(), dij.get(), aodv.get(), null);
        }
    }

    public record RaceResult(Route bfs, Route dijkstra, Route aodv, Throwable failure) {
    }
}
