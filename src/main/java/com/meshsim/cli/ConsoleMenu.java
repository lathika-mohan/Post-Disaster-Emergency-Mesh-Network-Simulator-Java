package com.meshsim.cli;

import com.meshsim.exception.MeshSimulationException;
import com.meshsim.exception.NodeUnreachableException;
import com.meshsim.io.DatasetLoader;
import com.meshsim.io.ReportWriter;
import com.meshsim.model.*;
import com.meshsim.network.MeshNetwork;
import com.meshsim.routing.*;

import java.nio.file.Path;
import java.util.Scanner;

/** The original console front-end, kept working alongside the GUI (--cli vs --gui). */
public final class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("=".repeat(50));
        System.out.println("Post-Disaster Emergency Mesh Network Simulator");
        System.out.println("=".repeat(50));

        Scenario scenario = buildDemoScenario();
        MeshNetwork network = new MeshNetwork(scenario);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().strip();
            switch (choice) {
                case "1" -> printScenario(scenario);
                case "2" -> routeMessage(network, "BFS");
                case "3" -> routeMessage(network, "Dijkstra");
                case "4" -> routeMessage(network, "AODV");
                case "5" -> routeMessage(network, "DSR");
                case "6" -> routeMessage(network, "Epidemic");
                case "0" -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println("""

                1) Show scenario
                2) Route with BFS
                3) Route with Dijkstra
                4) Route with AODV
                5) Route with DSR
                6) Route with Epidemic
                0) Exit
                Choose:\s""");
    }

    private void printScenario(Scenario scenario) {
        System.out.println(scenario);
        for (Node n : scenario.nodes()) {
            System.out.println("  " + n);
        }
        for (Obstacle o : scenario.obstacles()) {
            System.out.println("  " + Obstacle.describe(o));
        }
    }

    private void routeMessage(MeshNetwork network, String protocolName) {
        System.out.print("Source node id: ");
        String src = scanner.nextLine().strip();
        System.out.print("Destination node id: ");
        String dst = scanner.nextLine().strip();

        Router router = switch (protocolName) {
            case "BFS" -> new BFSRouter(network);
            case "Dijkstra" -> new DijkstraRouter(network);
            case "AODV" -> new AODVRouter(network);
            case "DSR" -> new DSRRouter(network);
            case "Epidemic" -> new EpidemicRouter(network);
            default -> throw new IllegalStateException("Unknown protocol: " + protocolName);
        };

        try {
            Route route = router.findRoute(src, dst);
            System.out.println(ReportWriter.buildReport(network.scenario(), route, 1.0, route.hopCount()));
        } catch (NodeUnreachableException e) {
            System.out.println("Routing failed: " + e.getMessage());
        }
    }

    private Scenario buildDemoScenario() {
        Scenario scenario = new Scenario("demo", "Earthquake Zone Demo", 400, 300);
        scenario.addNode(Node.withId("N1", NodeType.BASE_STATION, new Point(20, 20), 1.0));
        scenario.addNode(Node.withId("N2", NodeType.STATIC_RELAY, new Point(60, 40), 1.0));
        scenario.addNode(Node.withId("N3", NodeType.SURVIVOR, new Point(100, 60), 0.8));
        scenario.addNode(Node.withId("N4", NodeType.RESCUE_TEAM, new Point(140, 85), 1.0));
        scenario.addNode(Node.withId("N5", NodeType.SURVIVOR, new Point(180, 110), 0.6));
        scenario.addObstacle(new RubbleField(new Point(300, 200), 20, 0.6));
        return scenario;
    }
}
