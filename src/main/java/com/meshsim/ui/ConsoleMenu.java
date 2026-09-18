package com.meshsim.ui;

import com.meshsim.data.DatasetLoader;
import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.BFSRouter;
import com.meshsim.routing.DijkstraRouter;
import com.meshsim.routing.Route;
import com.meshsim.routing.Router;
import com.meshsim.simulation.Metrics;
import com.meshsim.simulation.ScenarioBuilder;
import com.meshsim.simulation.Simulator;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Text-menu driven console interface, styled as a disaster-response terminal. */
public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);
    private Simulator simulator;
    private final Router bfsRouter = new BFSRouter();
    private final Router dijkstraRouter = new DijkstraRouter();

    public void run() {
        Display.bootSequence();
        System.out.println("Press ENTER to start...");
        scanner.nextLine();

        simulator = new Simulator(ScenarioBuilder.relayFormation());
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": loadScenario(); break;
                case "2": loadDataset(); break;
                case "3": viewNetwork(); break;
                case "4": sendMessage(); break;
                case "5": failNode(); break;
                case "6": dashboard(); break;
                case "7": coverage(); break;
                case "8": addObstacle(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid option, try again.");
            }
        }
        missionSummary();
    }

    private void printMenu() {
        System.out.println();
        Display.thinDivider();
        System.out.printf("Active nodes: %d | Edges: %d | Tick: %d%n",
                simulator.getNetwork().activeNodeCount(), simulator.getNetwork().edgeCount(), simulator.getTick());
        System.out.println("1. Load predefined scenario");
        System.out.println("2. Load node dataset from CSV");
        System.out.println("3. View mesh network status");
        System.out.println("4. Send emergency message (choose routing protocol)");
        System.out.println("5. Simulate node failure (disaster event)");
        System.out.println("6. View live dashboard / performance metrics");
        System.out.println("7. Compute coverage ratio from a rescue node");
        System.out.println("8. Add obstacle (flood/fire/rubble/collapse)");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    private void loadScenario() {
        System.out.println("1) Isolated Survivor  2) Relay Formation  3) Flood Barrier  4) Self-Healing Network");
        System.out.print("Choose scenario: ");
        String c = scanner.nextLine().trim();
        MeshNetwork net;
        String name;
        switch (c) {
            case "1": net = ScenarioBuilder.isolatedSurvivor(); name = "Isolated Survivor"; break;
            case "3": net = ScenarioBuilder.floodBarrier(); name = "Flood Barrier"; break;
            case "4": net = ScenarioBuilder.selfHealingNetwork(); name = "Self-Healing Network"; break;
            default: net = ScenarioBuilder.relayFormation(); name = "Relay Formation";
        }
        net.rebuildAllLinks();
        simulator = new Simulator(net);

        Map<String, String> rows = Display.row();
        rows.put("Scenario", name);
        rows.put("Simulation Area", "1000 x 800 (units)");
        rows.put("Nodes Deployed", String.valueOf(net.totalNodeCount()));
        rows.put("Obstacles", String.valueOf(net.getEnvironment().getObstacles().size()));
        Display.section("DISASTER CONFIGURATION", rows);
        Display.progressBar("Network Connectivity", connectivityPercent(net));
        System.out.println("Simulation Created Successfully.");
    }

    private void loadDataset() {
        System.out.print("CSV file path (e.g. data/sample_nodes.csv): ");
        String path = scanner.nextLine().trim();
        try {
            Environment env = new Environment(1000, 800);
            MeshNetwork net = new MeshNetwork(env);
            int count = DatasetLoader.loadIntoNetwork(path, net);
            simulator = new Simulator(net);

            Map<String, String> rows = Display.row();
            rows.put("Nodes Loaded", String.valueOf(count));
            rows.put("Edges Formed", String.valueOf(net.edgeCount()));
            rows.put("Connected Components", String.valueOf(net.connectedComponents()));
            Display.section("NODE DISCOVERY", rows);
        } catch (Exception e) {
            System.out.println("Failed to load dataset: " + e.getMessage());
        }
    }

    private void viewNetwork() {
        MeshNetwork net = simulator.getNetwork();
        Display.divider();
        System.out.println("MESH NETWORK STATUS");
        Display.thinDivider();
        for (Node n : net.getNodes()) {
            List<com.meshsim.network.Link> links = net.neighborsOf(n.getId());
            StringBuilder sb = new StringBuilder();
            for (com.meshsim.network.Link l : links) sb.append(l.other(n).getId()).append(" ");
            System.out.printf("Node-%02d [%-8s] %-11s links -> %s%n",
                    n.getId(), n.getType(), n.isActive() ? "ONLINE" : "OFFLINE",
                    sb.length() == 0 ? "(none)" : sb.toString().trim());
        }
        Display.thinDivider();
        Display.progressBar("Network Connectivity", connectivityPercent(net));
        System.out.println("Connected Components : " + net.connectedComponents());
        Display.divider();

        if (!net.getEnvironment().getObstacles().isEmpty()) {
            System.out.println("Obstacles:");
            net.getEnvironment().getObstacles().forEach(o -> System.out.println("  " + o));
        }
    }

    private void sendMessage() {
        System.out.print("Source node id: ");
        int src = readInt();
        System.out.print("Destination node id: ");
        int dst = readInt();
        System.out.print("Routing protocol - 1) BFS  2) Dijkstra: ");
        String r = scanner.nextLine().trim();
        Router router = "2".equals(r) ? dijkstraRouter : bfsRouter;

        System.out.println();
        System.out.println("Searching Best Route...");
        Route route = simulator.sendMessage(src, dst, router);

        Display.divider();
        if (!route.isFound()) {
            Display.alertHeader("ROUTE NOT FOUND");
            System.out.printf("Source           : Node-%02d%n", src);
            System.out.printf("Destination      : Node-%02d%n", dst);
            System.out.println("Status           : MESSAGE DROPPED");
            Display.divider();
            return;
        }

        List<Node> path = route.getPath();
        for (int i = 0; i < path.size(); i++) {
            System.out.println((i == 0 ? "Source        : " : i == path.size() - 1 ? "Destination   : " : "  via         : ")
                    + "Node-" + String.format("%02d", path.get(i).getId()) + " [" + path.get(i).getType() + "]");
            if (i < path.size() - 1) System.out.println("     |");
        }
        Display.thinDivider();
        double estimatedDelaySec = route.getHopCount() * 0.15;
        System.out.printf("Routing Algorithm : %s%n", router.name());
        System.out.printf("Total Distance    : %.1f units%n", route.getTotalCost());
        System.out.printf("Hop Count         : %d%n", route.getHopCount());
        System.out.printf("Estimated Delay   : %.2f sec (approx.)%n", estimatedDelaySec);
        System.out.println("Route Status      : SUCCESS");
        Display.divider();
    }

    private void failNode() {
        System.out.print("Node id to fail: ");
        int id = readInt();

        Display.alertHeader("NODE-" + String.format("%02d", id) + " FAILURE DETECTED");
        System.out.println("Removing Node...");
        System.out.println("Updating Graph...");
        long recoveryMs = simulator.failNode(id);
        if (recoveryMs < 0) {
            System.out.println("No such node: " + id);
            return;
        }
        System.out.println("Searching Backup Route...");
        Display.progressBar("Recovery Progress", 100);
        System.out.println();
        Map<String, String> rows = Display.row();
        rows.put("Recovery Time", recoveryMs + " ms");
        rows.put("Active Nodes", String.valueOf(simulator.getNetwork().activeNodeCount()));
        rows.put("Edges Remaining", String.valueOf(simulator.getNetwork().edgeCount()));
        rows.put("Connected Components", String.valueOf(simulator.getNetwork().connectedComponents()));
        rows.put("Status", simulator.getNetwork().connectedComponents() <= 1 ? "COMMUNICATION RESTORED" : "NETWORK PARTITIONED");
        Display.section("FAILURE RECOVERY REPORT", rows);
    }

    private void dashboard() {
        Metrics m = simulator.getMetrics();
        double coverage = simulator.overallCoverageRatio(dijkstraRouter);
        MeshNetwork net = simulator.getNetwork();

        Map<String, String> rows = Display.row();
        rows.put("Survivors Connected", simulator.activeSurvivorCount() + " / " + simulator.survivorCount());
        rows.put("Coverage Ratio", String.format("%.1f%%", coverage));
        rows.put("Packets Sent", String.valueOf(m.getPacketsSent()));
        rows.put("Packets Delivered", String.valueOf(m.getPacketsDelivered()));
        rows.put("Packet Delivery Ratio", String.format("%.1f%%", m.packetDeliveryRatio()));
        rows.put("Average Hop Count", String.format("%.2f", m.averageHopCount()));
        rows.put("Energy Consumed", String.format("%.6f J", m.getEnergyConsumedJoules()));
        rows.put("Energy Efficiency", String.format("%.2f deliveries/J", m.energyEfficiency()));
        rows.put("Network Lifetime", String.format("%.1f%%", simulator.networkLifetimePercent()));
        rows.put("Avg Recovery Time", String.format("%.1f ms", m.averageRecoveryTimeMs()));
        Display.section("LIVE DISASTER DASHBOARD", rows);

        Map<String, String> graph = Display.row();
        graph.put("Vertices", String.valueOf(net.activeNodeCount()));
        graph.put("Edges", String.valueOf(net.edgeCount()));
        graph.put("Connected Components", String.valueOf(net.connectedComponents()));
        graph.put("Average Degree", String.format("%.2f", net.averageDegree()));
        graph.put("Graph Density", String.format("%.3f", net.density()));
        Display.section("GRAPH STATISTICS", graph);
    }

    private void coverage() {
        System.out.print("Rescue node id: ");
        int id = readInt();
        double c = simulator.coverageRatio(id, dijkstraRouter);
        System.out.printf("Coverage ratio from node %d: %.1f%%%n", id, c);
    }

    private void addObstacle() {
        System.out.println("1) Flood Zone 2) Fire Region 3) Rubble Field 4) Building Collapse");
        String t = scanner.nextLine().trim();
        Obstacle.Type type;
        switch (t) {
            case "2": type = Obstacle.Type.FIRE_REGION; break;
            case "3": type = Obstacle.Type.RUBBLE_FIELD; break;
            case "4": type = Obstacle.Type.BUILDING_COLLAPSE; break;
            default: type = Obstacle.Type.FLOOD_ZONE;
        }
        System.out.print("x: "); double x = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("y: "); double y = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("radius: "); double rad = Double.parseDouble(scanner.nextLine().trim());
        simulator.getNetwork().getEnvironment().addObstacle(new Obstacle(type, x, y, rad));
        simulator.getNetwork().rebuildAllLinks();
        System.out.println("Obstacle added and network links recalculated.");
    }

    private void missionSummary() {
        Metrics m = simulator.getMetrics();
        Map<String, String> rows = Display.row();
        rows.put("Nodes Deployed", String.valueOf(simulator.getNetwork().totalNodeCount()));
        rows.put("Messages Routed", String.valueOf(m.getPacketsSent()));
        rows.put("Successful Deliveries", String.valueOf(m.getPacketsDelivered()));
        rows.put("Failed Deliveries", String.valueOf(m.getPacketsSent() - m.getPacketsDelivered()));
        rows.put("Coverage Ratio", String.format("%.1f%%", simulator.overallCoverageRatio(dijkstraRouter)));
        rows.put("Network Lifetime", String.format("%.1f%%", simulator.networkLifetimePercent()));
        rows.put("Status", m.getPacketsSent() == 0 ? "NO MESSAGES SENT" :
                m.packetDeliveryRatio() >= 50 ? "DISASTER RESPONSE SUCCESSFUL" : "RESPONSE DEGRADED");
        Display.section("MISSION SUMMARY", rows);
        System.out.println();
        System.out.println("Thank you for using");
        System.out.println("POST-DISASTER EMERGENCY MESH NETWORK SIMULATOR");
    }

    private double connectivityPercent(MeshNetwork net) {
        long v = net.activeNodeCount();
        if (v <= 1) return v == 1 ? 100 : 0;
        int maxEdges = (int) (v * (v - 1) / 2);
        return maxEdges == 0 ? 0 : (double) net.edgeCount() / maxEdges * 100.0;
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer: ");
            }
        }
    }
}
